package com.example.demo.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.example.demo.constants.ErrorCodeEnum;
import com.example.demo.exception.PaymentProcessingException;
import com.example.demo.pojo.CreateOrderReq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentValidator {
	public void validateCreateReq(CreateOrderReq createOrderReq) {
		if (createOrderReq == null) {
			log.error("CreateOrderReq cannot be null");

			throw new PaymentProcessingException(ErrorCodeEnum.CREATE_PAYMENT_ERROR.getErrorCode(),
					ErrorCodeEnum.CREATE_PAYMENT_ERROR.getErrorMessage(), HttpStatus.BAD_REQUEST);
		}

		if (createOrderReq.getPaymentMethodId() != 1) {
			log.error("CreateOrderReq || PaymentMethodId is invalid ");

			throw new PaymentProcessingException(ErrorCodeEnum.INVALID_PAYMENT_METHOD.getErrorCode(),
					ErrorCodeEnum.INVALID_PAYMENT_METHOD.getErrorMessage(), HttpStatus.BAD_REQUEST);
		}

		if (createOrderReq.getPaymentTypeId() != 1) {
			log.error("CreateOrderReq || PaymentTypeId is invalid ");

			throw new PaymentProcessingException(ErrorCodeEnum.INVALID_PAYMENT_TYPE.getErrorCode(),
					ErrorCodeEnum.INVALID_PAYMENT_TYPE.getErrorMessage(), HttpStatus.BAD_REQUEST);
		}

		if (createOrderReq.getProviderId() != 1) {
			log.error("CreateOrderReq || ProviderId is invalid");

			throw new PaymentProcessingException(ErrorCodeEnum.INVALID_PAYMENT_PROVIDER.getErrorCode(),
					ErrorCodeEnum.INVALID_PAYMENT_PROVIDER.getErrorMessage(), HttpStatus.BAD_REQUEST);
		}

		if (createOrderReq.getAmount() <= 0) {
			log.error("CreateOrderReq || Amount is invalid, cannot be null or negative");

			throw new PaymentProcessingException(ErrorCodeEnum.INVALID_AMOUNT.getErrorCode(),
					ErrorCodeEnum.INVALID_AMOUNT.getErrorMessage(), HttpStatus.BAD_REQUEST);
		}

		if (createOrderReq.getCurrency() == null) {
			log.error("CreateOrderReq || Currency cannot be null");

			throw new PaymentProcessingException(ErrorCodeEnum.INVALID_CURRENCY.getErrorCode(),
					ErrorCodeEnum.INVALID_CURRENCY.getErrorMessage(), HttpStatus.BAD_REQUEST);
		}

		if (createOrderReq.getUserId() == null) {
			log.error("CreateOrderReq || UserId cannot be null");

			throw new PaymentProcessingException(ErrorCodeEnum.INVALID_USERID.getErrorCode(),
					ErrorCodeEnum.INVALID_USERID.getErrorMessage(), HttpStatus.BAD_REQUEST);
		}
	}
}
