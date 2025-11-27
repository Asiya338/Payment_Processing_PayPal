package com.example.demo.service.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

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

import com.example.demo.constants.Constant;
import com.example.demo.constants.ErrorCodeEnum;
import com.example.demo.dto.TransactionDto;
import com.example.demo.exception.PaymentProcessingException;
import com.example.demo.http.HttpRequest;
import com.example.demo.paypalprovider.PPCaptureOrderRes;
import com.example.demo.paypalprovider.PPErrorResponse;
import com.example.demo.service.impl.helper.PPCaptureOrderHelper;
import com.example.demo.utils.JsonUtil;

@ExtendWith(MockitoExtension.class)
class PPCaptureOrderHelperTest {

	@Mock
	private JsonUtil jsonUtil;;

	@InjectMocks
	private PPCaptureOrderHelper ppCaptureOrderHelper;

	@Test
	void prepareHttpRequestTest() {

		// Arrange
		TransactionDto txnDto = new TransactionDto();
		txnDto.setProviderReference("ORD_1234");

		String txnReference = "TXN123456789012345678901234567890123";

		// Inject @Value field into helper class
		ReflectionTestUtils.setField(ppCaptureOrderHelper, "paypalCaptureOrderUrl",
				"https://api.paypal.com/v2/checkout/orders/{orderId}/capture");

		// Act
		HttpRequest httpRequest = ppCaptureOrderHelper.prepareHttpRequest(txnDto);

		// Assert
		assertNotNull(httpRequest);
		assertNotNull(httpRequest.getHeaders());

		// Validate URL replacement
		assertEquals("https://api.paypal.com/v2/checkout/orders/ORD_1234/capture", httpRequest.getUrl());

		// Validate method = POST
		assertEquals(HttpMethod.POST, httpRequest.getHttpMethod());

		// Validate body = NULL_BODY
		assertEquals(Constant.NULL_BODY, httpRequest.getBody());

		// Validate content type = application/json
		assertEquals(MediaType.APPLICATION_JSON, httpRequest.getHeaders().getContentType());
	}

	@Test
	void prepareResponse_success() {

		// Arrange
		ResponseEntity<String> httpResponse = ResponseEntity.status(HttpStatus.OK).body("{json-body}");

		PPCaptureOrderRes mockRes = new PPCaptureOrderRes();
		mockRes.setOrderId("ORD_1234");
		mockRes.setPaymentStatus(Constant.COMPLETED);

		when(jsonUtil.fromJson("{json-body}", PPCaptureOrderRes.class)).thenReturn(mockRes);

		// Act
		PPCaptureOrderRes result = ppCaptureOrderHelper.prepareHttpResponse(httpResponse);

		// Assert
		assertNotNull(result);
		assertEquals("ORD_1234", result.getOrderId());
		assertEquals(Constant.COMPLETED, result.getPaymentStatus());
	}

	@Test
	void prepareResponse_error4xx() {

		// Arrange
		ResponseEntity<String> httpResponse = ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{error-json}");

		PPErrorResponse mockError = new PPErrorResponse("20002", "Invalid Order ID");

		when(jsonUtil.fromJson("{error-json}", PPErrorResponse.class)).thenReturn(mockError);

		// Act + Assert
		PaymentProcessingException ex = assertThrows(PaymentProcessingException.class,
				() -> ppCaptureOrderHelper.prepareHttpResponse(httpResponse));

		assertEquals("20002", ex.getErrorCode());
		assertEquals("Invalid Order ID", ex.getErrorMessage());
		assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus());
	}

	@Test
	void prepareResponse_unknownError() {

		// Arrange
		ResponseEntity<String> httpResponse = ResponseEntity.status(HttpStatus.OK).body("{json-body}");

		// Simulate invalid response (missing orderId or wrong status)
		PPCaptureOrderRes invalidRes = new PPCaptureOrderRes();
		invalidRes.setOrderId(null); // invalid
		invalidRes.setPaymentStatus("PENDING");

		when(jsonUtil.fromJson("{json-body}", PPCaptureOrderRes.class)).thenReturn(invalidRes);

		// Act + Assert
		PaymentProcessingException ex = assertThrows(PaymentProcessingException.class,
				() -> ppCaptureOrderHelper.prepareHttpResponse(httpResponse));

		assertEquals(ErrorCodeEnum.PAYPAL_PROVIDER_UNKNOWN_ERROR.getErrorCode(), ex.getErrorCode());
		assertEquals(ErrorCodeEnum.PAYPAL_PROVIDER_UNKNOWN_ERROR.getErrorMessage(), ex.getErrorMessage());
		assertEquals(HttpStatus.BAD_GATEWAY, ex.getHttpStatus());
	}

}
