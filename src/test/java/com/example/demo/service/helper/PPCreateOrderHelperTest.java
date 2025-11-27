package com.example.demo.service.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.demo.constants.ErrorCodeEnum;
import com.example.demo.dto.TransactionDto;
import com.example.demo.exception.PaymentProcessingException;
import com.example.demo.http.HttpRequest;
import com.example.demo.paypalprovider.PPCreateOrderReq;
import com.example.demo.paypalprovider.PPErrorResponse;
import com.example.demo.paypalprovider.PPInitiatePayRes;
import com.example.demo.pojo.InitiateOrderReq;
import com.example.demo.service.impl.helper.PPCreateOrderHelper;
import com.example.demo.utils.JsonUtil;

@ExtendWith(MockitoExtension.class)
class PPCreateOrderHelperTest {

	@Mock
	private JsonUtil jsonUtil;

	@InjectMocks
	private PPCreateOrderHelper ppCreateOrderHelper;

	@BeforeEach
	void setup() {
		// Inject @Value property
		ReflectionTestUtils.setField(ppCreateOrderHelper, "paypalCreateOrderUrl",
				"https://api.paypal.com/v2/checkout/orders");
	}

	@Test
	void prepareHttpRequestTest() {

		// Arrange
		TransactionDto txnDto = new TransactionDto();
		txnDto.setAmount(BigDecimal.valueOf(99.0));
		txnDto.setCurrency("USD");

		InitiateOrderReq initiatePaymentReq = new InitiateOrderReq();
		initiatePaymentReq.setSuccessUrl("https://merchant/success");
		initiatePaymentReq.setCancelUrl("https://merchant/cancel");

		String txnReference = "TXN123456789012345678901234567890123";

		// Mock JSON output
		String jsonBody = "{mock-json}";
		when(jsonUtil.toJson(any(PPCreateOrderReq.class))).thenReturn(jsonBody);

		// Act
		HttpRequest httpRequest = ppCreateOrderHelper.prepareHttpRequest(txnDto, initiatePaymentReq);

		// Assert
		assertNotNull(httpRequest);
		assertEquals(HttpMethod.POST, httpRequest.getHttpMethod());
		assertEquals("https://api.paypal.com/v2/checkout/orders", httpRequest.getUrl());
		assertEquals(jsonBody, httpRequest.getBody());
		assertEquals(MediaType.APPLICATION_JSON, httpRequest.getHeaders().getContentType());
	}

	@Test
	void prepareResponse_success() {

		// Arrange
		ResponseEntity<String> httpResponse = ResponseEntity.status(HttpStatus.OK).body("{json}");

		PPInitiatePayRes mockRes = new PPInitiatePayRes();
		mockRes.setOrderId("ORD_1234");
		mockRes.setPaymentStatus("PAYER_ACTION_REQUIRED");
		mockRes.setRedirectUrl("https://paypal.com/approve");

		when(jsonUtil.fromJson("{json}", PPInitiatePayRes.class)).thenReturn(mockRes);

		// Act
		PPInitiatePayRes result = ppCreateOrderHelper.prepareResponse(httpResponse);

		// Assert
		assertNotNull(result);
		assertEquals("ORD_1234", result.getOrderId());
		assertEquals("PAYER_ACTION_REQUIRED", result.getPaymentStatus());
		assertEquals("https://paypal.com/approve", result.getRedirectUrl());
	}

	// --------------------------------------------------------------------
	// TEST 3: prepareResponse() 4xx/5xx → Throws Exception
	// --------------------------------------------------------------------
	@Test
	void prepareResponse_error4xx() {

		// Arrange
		ResponseEntity<String> httpResponse = ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{error-json}");

		PPErrorResponse error = new PPErrorResponse("20002", "Invalid request");

		when(jsonUtil.fromJson("{error-json}", PPErrorResponse.class)).thenReturn(error);

		// Act + Assert
		PaymentProcessingException ex = assertThrows(PaymentProcessingException.class,
				() -> ppCreateOrderHelper.prepareResponse(httpResponse));

		assertEquals("20002", ex.getErrorCode());
		assertEquals("Invalid request", ex.getErrorMessage());
		assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus());
	}

	@Test
	void prepareResponse_unknownError() {

		// Arrange
		ResponseEntity<String> httpResponse = ResponseEntity.status(HttpStatus.OK).body("{json}");

		PPInitiatePayRes invalidRes = new PPInitiatePayRes();
		invalidRes.setOrderId(null); // invalid → triggers unknown error
		invalidRes.setPaymentStatus(null);
		invalidRes.setRedirectUrl(null);

		when(jsonUtil.fromJson("{json}", PPInitiatePayRes.class)).thenReturn(invalidRes);

		// Act + Assert
		PaymentProcessingException ex = assertThrows(PaymentProcessingException.class,
				() -> ppCreateOrderHelper.prepareResponse(httpResponse));

		assertEquals(ErrorCodeEnum.PAYPAL_PROVIDER_UNKNOWN_ERROR.getErrorCode(), ex.getErrorCode());
		assertEquals(ErrorCodeEnum.PAYPAL_PROVIDER_UNKNOWN_ERROR.getErrorMessage(), ex.getErrorMessage());
		assertEquals(HttpStatus.BAD_GATEWAY, ex.getHttpStatus());
	}
}
