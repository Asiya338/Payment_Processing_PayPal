package com.example.demo.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(PaymentProcessingException.class)
	public void handleProcessingException(PaymentProcessingException ex) {

	}

	@ExceptionHandler(NoResourceFoundException.class)
	public void handleProcessingException(NoResourceFoundException ex) {

	}

	@ExceptionHandler(Exception.class)
	public void handleGenericException(Exception ex) {

	}
}
