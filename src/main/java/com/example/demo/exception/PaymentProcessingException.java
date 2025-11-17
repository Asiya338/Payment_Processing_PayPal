package com.example.demo.exception;

import org.springframework.http.HttpStatus;

import lombok.Data;

@Data
public class PaymentProcessingException extends Exception {

	private final String errorCode;
	private final String errorMessage;
	private final HttpStatus httpStatus;

	public PaymentProcessingException(String errorCode, String errorMessage, HttpStatus httpStatus) {
		super(errorMessage);
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
		this.httpStatus = httpStatus;
	}

}
