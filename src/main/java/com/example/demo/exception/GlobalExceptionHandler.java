package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.demo.constants.ErrorCodeEnum;
import com.example.demo.paypalprovider.PPErrorResponse;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(PaymentProcessingException.class)
	public ResponseEntity<PPErrorResponse> handlePayPalProviderException(PaymentProcessingException ex) {
		log.error("Handling PaymentProcessingException: {}", ex.getErrorMessage(), ex);

		PPErrorResponse errorResponse = new PPErrorResponse(ex.getErrorCode(), ex.getErrorMessage());

		return ResponseEntity.status(ex.getHttpStatus()).body(errorResponse);
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<PPErrorResponse> resourceNotFoundEception(ResourceNotFoundException ex) {
		log.error("Handling ResourceNotFound Exception: {}", ex.getErrorMessage(), ex);

		PPErrorResponse errorResponse = new PPErrorResponse(ex.getErrorCode(), ex.getErrorMessage());

		return ResponseEntity.status(ex.getHttpStatus()).body(errorResponse);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<PPErrorResponse> handleGenericException(Exception ex) {
		log.error("Handling Exception : {} ", ex.getMessage(), ex);

		PPErrorResponse errorResponse = new PPErrorResponse(ErrorCodeEnum.GENERIC_ERROR.getErrorCode(),
				ErrorCodeEnum.GENERIC_ERROR.getErrorMessage());

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
	}
}
