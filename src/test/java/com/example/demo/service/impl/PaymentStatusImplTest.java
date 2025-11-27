package com.example.demo.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.demo.constants.Constant;
import com.example.demo.dao.interfaces.TransactionDao;
import com.example.demo.dto.TransactionDto;
import com.example.demo.entity.TransactionEntity;
import com.example.demo.exception.PaymentProcessingException;
import com.example.demo.http.HttpRequest;
import com.example.demo.http.HttpServiceEngine;
import com.example.demo.paypalprovider.PPCaptureOrderRes;
import com.example.demo.paypalprovider.PPInitiatePayRes;
import com.example.demo.pojo.CreateOrderReq;
import com.example.demo.pojo.InitiateOrderReq;
import com.example.demo.pojo.PaymentResponse;
import com.example.demo.service.PaymentStatusProcessor;
import com.example.demo.service.PaymentValidator;
import com.example.demo.service.impl.helper.PPCaptureOrderHelper;
import com.example.demo.service.impl.helper.PPCreateOrderHelper;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

	@Mock
	private ModelMapper modelMapper;
	@Mock
	private PaymentStatusProcessor paymentStatusProcessor;
	@Mock
	private PaymentValidator paymentValidator;
	@Mock
	private TransactionDao transactionDao;
	@Mock
	private HttpServiceEngine httpServiceEngine;
	@Mock
	private PPCreateOrderHelper createOrderHelper;
	@Mock
	private PPCaptureOrderHelper ppCaptureOrderHelper;

	@InjectMocks
	private PaymentServiceImpl paymentServiceImpl;

	@Test
	void testCreatePayment_success() {
		CreateOrderReq req = new CreateOrderReq();
		TransactionDto txnDto = new TransactionDto();
		txnDto.setTxnReference(UUID.randomUUID().toString());
		txnDto.setTxnStatusId(Constant.CREATED);

		when(modelMapper.map(req, TransactionDto.class)).thenReturn(txnDto);
		when(paymentStatusProcessor.processPayment(txnDto)).thenReturn(txnDto);

		PaymentResponse res = paymentServiceImpl.createPayment(req);

		assertNotNull(res);
		assertEquals(Constant.CREATED, res.getTxnStatusId());
		assertEquals(36, res.getTxnReference().length());
		verify(paymentValidator).validateCreateReq(req);
		verify(paymentStatusProcessor).processPayment(txnDto);
	}

	@Test
	void testCreatePayment_validationFailure() {
		CreateOrderReq req = new CreateOrderReq();

		doThrow(new PaymentProcessingException("1001", "Invalid", HttpStatus.BAD_REQUEST)).when(paymentValidator)
				.validateCreateReq(req);

		assertThrows(PaymentProcessingException.class, () -> paymentServiceImpl.createPayment(req));
	}

	@Test
	void testInitiatePayment_success() {
		String txnRef = "TXN123";
		InitiateOrderReq initReq = new InitiateOrderReq();

		TransactionEntity txnEntity = new TransactionEntity();
		txnEntity.setTxnReference(txnRef);

		TransactionDto txnDto = new TransactionDto();
		txnDto.setTxnReference(txnRef);

		PPInitiatePayRes ppRes = new PPInitiatePayRes();
		ppRes.setOrderId("ORD_1");
		ppRes.setRedirectUrl("https://pay");

		HttpRequest mockReq = new HttpRequest();
		ResponseEntity<String> httpResponse = ResponseEntity.ok("success");

		when(transactionDao.getTransactionByTxnReference(txnRef)).thenReturn(txnEntity);
		when(modelMapper.map(txnEntity, TransactionDto.class)).thenReturn(txnDto);
		when(paymentStatusProcessor.processPayment(txnDto)).thenReturn(txnDto);
		when(createOrderHelper.prepareHttpRequest(txnDto, initReq)).thenReturn(mockReq);
		when(httpServiceEngine.makeHttpCall(mockReq)).thenReturn(httpResponse);
		when(createOrderHelper.prepareResponse(httpResponse)).thenReturn(ppRes);

		PaymentResponse response = paymentServiceImpl.initiatePayment(txnRef, initReq);

		assertNotNull(response);
		assertEquals(Constant.PENDING, response.getTxnStatusId());
		assertEquals("ORD_1", response.getProviderReference());
		verify(paymentValidator).validateInitiateOrderReq(txnRef, initReq);
		verify(paymentStatusProcessor, times(2)).processPayment(txnDto);
	}

	@Test
	void testInitiatePayment_processingException() {
		String txnRef = "TXN123";
		InitiateOrderReq initReq = new InitiateOrderReq();

		TransactionEntity txnEntity = new TransactionEntity();
		TransactionDto txnDto = new TransactionDto();

		when(transactionDao.getTransactionByTxnReference(txnRef)).thenReturn(txnEntity);
		when(modelMapper.map(txnEntity, TransactionDto.class)).thenReturn(txnDto);
		when(paymentStatusProcessor.processPayment(txnDto)).thenReturn(txnDto);

		when(createOrderHelper.prepareHttpRequest(txnDto, initReq))
				.thenThrow(new PaymentProcessingException("20002", "Provider error", HttpStatus.SERVICE_UNAVAILABLE));

		PaymentProcessingException ex = assertThrows(PaymentProcessingException.class,
				() -> paymentServiceImpl.initiatePayment(txnRef, initReq));

		assertEquals("20002", ex.getErrorCode());
		verify(paymentStatusProcessor, times(2)).processPayment(txnDto);
	}

	@Test
	void testInitiatePayment_unexpectedError() {
		String txnRef = "TXN123";
		InitiateOrderReq initReq = new InitiateOrderReq();

		TransactionEntity txnEntity = new TransactionEntity();
		TransactionDto txnDto = new TransactionDto();

		when(transactionDao.getTransactionByTxnReference(txnRef)).thenReturn(txnEntity);
		when(modelMapper.map(txnEntity, TransactionDto.class)).thenReturn(txnDto);
		when(paymentStatusProcessor.processPayment(txnDto)).thenReturn(txnDto);

		when(createOrderHelper.prepareHttpRequest(txnDto, initReq)).thenThrow(new RuntimeException("Unexpected"));

		assertThrows(RuntimeException.class, () -> paymentServiceImpl.initiatePayment(txnRef, initReq));

		verify(paymentStatusProcessor, times(2)).processPayment(txnDto);
	}

	@Test
	void testCapturePayment_success() {
		String txnRef = "TXN123";

		TransactionEntity txnEntity = new TransactionEntity();
		txnEntity.setTxnReference(txnRef);

		TransactionDto txnDto = new TransactionDto();
		txnDto.setTxnReference(txnRef);

		HttpRequest mockReq = new HttpRequest();
		ResponseEntity<String> mockResponse = ResponseEntity.ok("success");

		PPCaptureOrderRes captureRes = new PPCaptureOrderRes();
		captureRes.setOrderId("CAP_1");

		when(transactionDao.getTransactionByTxnReference(txnRef)).thenReturn(txnEntity);
		when(modelMapper.map(txnEntity, TransactionDto.class)).thenReturn(txnDto);
		when(paymentStatusProcessor.processPayment(txnDto)).thenReturn(txnDto);
		when(ppCaptureOrderHelper.prepareHttpRequest(txnDto)).thenReturn(mockReq);
		when(httpServiceEngine.makeHttpCall(mockReq)).thenReturn(mockResponse);
		when(ppCaptureOrderHelper.prepareHttpResponse(mockResponse)).thenReturn(captureRes);

		PaymentResponse response = paymentServiceImpl.capturePayment(txnRef);

		assertNotNull(response);
		assertEquals(Constant.SUCCESS, response.getTxnStatusId());
		assertEquals(txnRef, response.getTxnReference());
		verify(paymentStatusProcessor, times(2)).processPayment(txnDto);
	}

	@Test
	void testCapturePayment_processingException() {
		String txnRef = "TXN123";

		TransactionEntity txnEntity = new TransactionEntity();
		TransactionDto txnDto = new TransactionDto();

		when(transactionDao.getTransactionByTxnReference(txnRef)).thenReturn(txnEntity);
		when(modelMapper.map(txnEntity, TransactionDto.class)).thenReturn(txnDto);
		when(ppCaptureOrderHelper.prepareHttpRequest(txnDto))
				.thenThrow(new PaymentProcessingException("20002", "Provider Error", HttpStatus.BAD_GATEWAY));

		assertThrows(PaymentProcessingException.class, () -> paymentServiceImpl.capturePayment(txnRef));
	}

	@Test
	void testCapturePayment_unexpectedError() {
		String txnRef = "TXN123";

		TransactionEntity txnEntity = new TransactionEntity();
		TransactionDto txnDto = new TransactionDto();

		when(transactionDao.getTransactionByTxnReference(txnRef)).thenReturn(txnEntity);
		when(modelMapper.map(txnEntity, TransactionDto.class)).thenReturn(txnDto);
		when(ppCaptureOrderHelper.prepareHttpRequest(txnDto)).thenThrow(new RuntimeException("Unexpected"));

		assertThrows(RuntimeException.class, () -> paymentServiceImpl.capturePayment(txnRef));
	}

	@Test
	void testGetPaymentsByUserId() {
		String userId = "USER_1";

		TransactionEntity t1 = new TransactionEntity();
		TransactionEntity t2 = new TransactionEntity();

		PaymentResponse mapped1 = new PaymentResponse();
		PaymentResponse mapped2 = new PaymentResponse();

		List<TransactionEntity> list = Arrays.asList(t1, t2);

		when(transactionDao.getTransactionsByUserId(userId)).thenReturn(list);
		when(modelMapper.map(t1, PaymentResponse.class)).thenReturn(mapped1);
		when(modelMapper.map(t2, PaymentResponse.class)).thenReturn(mapped2);

		List<PaymentResponse> response = paymentServiceImpl.getPaymentsByUserId(userId);

		assertEquals(2, response.size());
		verify(transactionDao).getTransactionsByUserId(userId);
	}
}
