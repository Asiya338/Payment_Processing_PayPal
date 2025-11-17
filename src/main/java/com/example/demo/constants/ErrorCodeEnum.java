package com.example.demo.constants;

import lombok.Getter;

@Getter
public enum ErrorCodeEnum {

	GENERIC_ERROR("20000", "Something went wrong. Please try later"),
	RESOURCE_NOT_FOUND("20001", "Resource not found, Please try with correct request url..."),
	PAYPAL_PROVIDER_SERVICE_UNAVAILABLE("20002",
			"Paypal Provider service unavailable," + " please try after some time...."),
	NO_STATUS_PROCESSOR_FOUND("20003", "No status processor found..."),
	CREATE_PAYMENT_ERROR("20004", "Create Payment Request cannot be null..."),
	INVALID_PAYMENT_METHOD("20005", "Payment Method must be valid and not null"),
	INVALID_PAYMENT_TYPE("20006", "Payment Type must be valid and not null"),
	INVALID_PAYMENT_PROVIDER("20007", "Payment Provider must be valid and not null"),
	TXN_REFERENCE_ERROR("20008", "Transaction reference must not ne null or empty"),
	INITIATE_PAYMENT_ERROR("20009", "Initiate Payment request must not be null.."),
	INVALID_SUCCESS_URL("20010", "Success URL is invalid or null or empty.."),
	INVALID_CANCEL_URL("20010", "Cancel URL is invalid or null or empty.."),
	TO_JSON_ERROR("20011", "ERROR CONVERTING ... TO JSON"),
	FROM_JSON_ERROR("20012", "ERROR CONVERTING FROM JSON TO ..."),
	PAYPAL_PROVIDER_UNKNOWN_ERROR("20013", "PayPal Provider Unknown Error occured, please try later..."),
	ERROR_UPDATING_TRANSACTION("20014", "Error updating transaction details.."),
	CAPTURE_PAYMENT_ERROR("20015", "Error in processing Capture Payment request. please try after initiating payment.");

	private final String errorCode;
	private final String errorMessage;

	ErrorCodeEnum(String errorCode, String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}
}
