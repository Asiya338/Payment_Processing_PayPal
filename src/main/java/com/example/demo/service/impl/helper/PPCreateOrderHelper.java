package com.example.demo.service.impl.helper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.demo.constants.ErrorCodeEnum;
import com.example.demo.dto.TransactionDto;
import com.example.demo.exception.PaymentProcessingException;
import com.example.demo.http.HttpRequest;
import com.example.demo.paypalprovider.PPCreateOrderReq;
import com.example.demo.paypalprovider.PPErrorResponse;
import com.example.demo.paypalprovider.PPInitiatePayRes;
import com.example.demo.pojo.InitiateOrderReq;
import com.example.demo.utils.JsonUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PPCreateOrderHelper {

	private final JsonUtil jsonUtil;

	@Value("${paypalprovider.createodrer.url}")
	private String paypalCreateOrderUrl;

	public HttpRequest prepareHttpRequest(TransactionDto processedDto, InitiateOrderReq initiateOrderReq) {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		PPCreateOrderReq ppCreateOrderReq = new PPCreateOrderReq();
		ppCreateOrderReq.setAmount(processedDto.getAmount().doubleValue());
		ppCreateOrderReq.setCurrencyCode(processedDto.getCurrency());
		ppCreateOrderReq.setReturnurl(initiateOrderReq.getSuccessUrl());
		ppCreateOrderReq.setCancelUrl(initiateOrderReq.getCancelUrl());

		String reqAsJson = jsonUtil.toJson(ppCreateOrderReq);

		HttpRequest httpRequest = new HttpRequest();
		httpRequest.setHeaders(headers);
		httpRequest.setUrl(paypalCreateOrderUrl);
		httpRequest.setHttpMethod(HttpMethod.POST);
		httpRequest.setBody(reqAsJson);

		log.info("Prepared http request for initiating payment : {} ", reqAsJson);

		return httpRequest;
	}

	public PPInitiatePayRes prepareResponse(ResponseEntity<String> httpResponse) {
		log.info("HTTP RESPONSE for initiate payment : {}  ", httpResponse);

		if (httpResponse.getStatusCode().equals(HttpStatus.OK)) {
			log.info("Preparing SUCCESS RESPONSE for initiate Payment : {} ", httpResponse);

			PPInitiatePayRes initiateResponse = jsonUtil.fromJson(httpResponse.getBody(), PPInitiatePayRes.class);
			log.info("Initiate Payment Response with status PAYER_ACTION_REQUIRED : {}", initiateResponse);

			if (initiateResponse != null && initiateResponse.getOrderId() != null
					&& !initiateResponse.getOrderId().isEmpty() && initiateResponse.getPaymentStatus() != null
					&& !initiateResponse.getPaymentStatus().isEmpty() && initiateResponse.getRedirectUrl() != null
					&& !initiateResponse.getRedirectUrl().isEmpty()) {
				log.info("Parsed Initiate Payment response  : {} ", initiateResponse);

				return initiateResponse;

			} else {
				log.error("Failed to parse response ||initiateResponse... ");
			}
		}

		else if (httpResponse.getStatusCode().is4xxClientError() || httpResponse.getStatusCode().is5xxServerError()) {
			log.error("Recieved 4xx, 5xx client & server error || initiateResponse");

			PPErrorResponse ppErrorResponse = jsonUtil.fromJson(httpResponse.getBody(), PPErrorResponse.class);
			log.error(" PPErrorResponse : {}", ppErrorResponse);

			throw new PaymentProcessingException(ppErrorResponse.getErrorCode(), ppErrorResponse.getErrorMessage(),
					HttpStatus.valueOf(httpResponse.getStatusCode().value()));
		}

		log.error("Unknown error occured from PayPalProvider Service || HttpResponse : {} ", httpResponse);

		throw new PaymentProcessingException(ErrorCodeEnum.PAYPAL_PROVIDER_UNKNOWN_ERROR.getErrorCode(),
				ErrorCodeEnum.PAYPAL_PROVIDER_UNKNOWN_ERROR.getErrorMessage(), HttpStatus.BAD_GATEWAY);

	}
}
