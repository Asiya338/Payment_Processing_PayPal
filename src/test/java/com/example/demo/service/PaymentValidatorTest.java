package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.constants.ErrorCodeEnum;
import com.example.demo.exception.PaymentProcessingException;
import com.example.demo.pojo.CreateOrderReq;
import com.example.demo.pojo.InitiateOrderReq;

@ExtendWith(MockitoExtension.class)
class PaymentValidatorTest {

	@InjectMocks
	private PaymentValidator validator;

	@Test
	void testValidateCreateReq_success() {

		CreateOrderReq req = new CreateOrderReq(123, " ", 1, 1, 1, 100, "USD");

		assertDoesNotThrow(() -> validator.validateCreateReq(req));
	}

	@Test
	void testValidateCreateReq_nullRequest() {

		PaymentProcessingException ex = assertThrows(PaymentProcessingException.class,
				() -> validator.validateCreateReq(null));

		assertEquals(ErrorCodeEnum.CREATE_PAYMENT_ERROR.getErrorCode(), ex.getErrorCode());
	}

	@Test
	void testInvalidPaymentMethod() {

		CreateOrderReq req = new CreateOrderReq(123, " ", 2, 1, 1, 100, "USD");

		PaymentProcessingException ex = assertThrows(PaymentProcessingException.class,
				() -> validator.validateCreateReq(req));

		assertEquals(ErrorCodeEnum.INVALID_PAYMENT_METHOD.getErrorCode(), ex.getErrorCode());
	}

	@Test
	void testInvalidPaymentType() {

		CreateOrderReq req = new CreateOrderReq(123, " ", 1, 2, 1, 100, "USD");

		PaymentProcessingException ex = assertThrows(PaymentProcessingException.class,
				() -> validator.validateCreateReq(req));

		assertEquals(ErrorCodeEnum.INVALID_PAYMENT_TYPE.getErrorCode(), ex.getErrorCode());
	}

	@Test
	void testInvalidProviderId() {

		CreateOrderReq req = new CreateOrderReq(123, "", 1, 1, 2, 100, "USD");

		PaymentProcessingException ex = assertThrows(PaymentProcessingException.class,
				() -> validator.validateCreateReq(req));

		assertEquals(ErrorCodeEnum.INVALID_PAYMENT_PROVIDER.getErrorCode(), ex.getErrorCode());
	}

	@Test
	void testInvalidAmount() {

		CreateOrderReq req = new CreateOrderReq(123, " ", 1, 1, 1, -800, "USD");

		PaymentProcessingException ex = assertThrows(PaymentProcessingException.class,
				() -> validator.validateCreateReq(req));

		assertEquals(ErrorCodeEnum.INVALID_AMOUNT.getErrorCode(), ex.getErrorCode());
	}

	@Test
	void testInvalidCurrency() {

		CreateOrderReq req = new CreateOrderReq(123, " ", 1, 1, 1, 100, "..");

		PaymentProcessingException ex = assertThrows(PaymentProcessingException.class,
				() -> validator.validateCreateReq(req));

		assertEquals(ErrorCodeEnum.INVALID_CURRENCY.getErrorCode(), ex.getErrorCode());
	}

	@Test
	void testInvalidUserId() {

		CreateOrderReq req = new CreateOrderReq(-1023, " ", 1, 1, 1, 100, "USD");

		PaymentProcessingException ex = assertThrows(PaymentProcessingException.class,
				() -> validator.validateCreateReq(req));

		assertEquals(ErrorCodeEnum.INVALID_USERID.getErrorCode(), ex.getErrorCode());
	}

	@Test
	void testValidateInitiateReq_success() {

		InitiateOrderReq req = new InitiateOrderReq("https://merchant.com/success", "https://merchant.com/cancel");

		assertDoesNotThrow(() -> validator.validateInitiateOrderReq("TXN123", req));
	}

	@Test
	void testTxnReference_null() {

		InitiateOrderReq req = new InitiateOrderReq("success", "cancel");

		PaymentProcessingException ex = assertThrows(PaymentProcessingException.class,
				() -> validator.validateInitiateOrderReq(null, req));

		assertEquals(ErrorCodeEnum.TXN_REFERENCE_ERROR.getErrorCode(), ex.getErrorCode());
	}

	@Test
	void testTxnReference_empty() {

		InitiateOrderReq req = new InitiateOrderReq("success", "cancel");

		PaymentProcessingException ex = assertThrows(PaymentProcessingException.class,
				() -> validator.validateInitiateOrderReq("", req));

		assertEquals(ErrorCodeEnum.TXN_REFERENCE_ERROR.getErrorCode(), ex.getErrorCode());
	}

	@Test
	void testInitiateOrderReq_null() {

		PaymentProcessingException ex = assertThrows(PaymentProcessingException.class,
				() -> validator.validateInitiateOrderReq("TXN123", null));

		assertEquals(ErrorCodeEnum.INITIATE_PAYMENT_ERROR.getErrorCode(), ex.getErrorCode());
	}

	@Test
	void testSuccessUrl_null() {

		InitiateOrderReq req = new InitiateOrderReq(null, "cancel");

		PaymentProcessingException ex = assertThrows(PaymentProcessingException.class,
				() -> validator.validateInitiateOrderReq("TXN123", req));

		assertEquals(ErrorCodeEnum.INVALID_SUCCESS_URL.getErrorCode(), ex.getErrorCode());
	}

	@Test
	void testCancelUrl_null() {

		InitiateOrderReq req = new InitiateOrderReq("success", null);

		PaymentProcessingException ex = assertThrows(PaymentProcessingException.class,
				() -> validator.validateInitiateOrderReq("TXN123", req));

		assertEquals(ErrorCodeEnum.INVALID_CANCEL_URL.getErrorCode(), ex.getErrorCode());
	}
}
