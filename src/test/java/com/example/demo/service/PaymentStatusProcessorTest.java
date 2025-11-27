package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.constants.ErrorCodeEnum;
import com.example.demo.dto.TransactionDto;
import com.example.demo.exception.PaymentProcessingException;
import com.example.demo.service.factory.PaymentStatusFactory;
import com.example.demo.service.interfaces.TransactionStatusProcessor;

@ExtendWith(MockitoExtension.class)
class PaymentStatusProcessorTest {

	@Mock
	private PaymentStatusFactory paymentStatusFactory;

	@Mock
	private TransactionStatusProcessor processor;

	@InjectMocks
	private PaymentStatusProcessor paymentStatusProcessor;

	@Test
	public void processPaymentTest_success() {

		TransactionDto inputTxnDto = new TransactionDto();
		inputTxnDto.setTxnStatusId(1);

		TransactionDto processedDto = new TransactionDto();
		processedDto.setTxnStatusId(2);

		when(paymentStatusFactory.getStatusProcessor(1)).thenReturn(processor);

		// mocking processor → return processed result
		when(processor.processStatus(inputTxnDto)).thenReturn(processedDto);

		TransactionDto result = paymentStatusProcessor.processPayment(inputTxnDto);

		assertNotNull(result);
		assertEquals(2, result.getTxnStatusId());

		verify(paymentStatusFactory, times(1)).getStatusProcessor(1);
		verify(processor, times(1)).processStatus(inputTxnDto);
	}

	@Test
	public void processPaymentTest_noProcessorFound() {

		TransactionDto inputTxnDto = new TransactionDto();
		inputTxnDto.setTxnStatusId(100);

		when(paymentStatusFactory.getStatusProcessor(100)).thenReturn(null);

		PaymentProcessingException ex = assertThrows(PaymentProcessingException.class,
				() -> paymentStatusProcessor.processPayment(inputTxnDto));

		assertEquals(ErrorCodeEnum.NO_STATUS_PROCESSOR_FOUND.getErrorCode(), ex.getErrorCode());
		assertEquals(ErrorCodeEnum.NO_STATUS_PROCESSOR_FOUND.getErrorMessage(), ex.getErrorMessage());

		verify(paymentStatusFactory, times(1)).getStatusProcessor(100);
		verify(processor, never()).processStatus(any());
	}

}
