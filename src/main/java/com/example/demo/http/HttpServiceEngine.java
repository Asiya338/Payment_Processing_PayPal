package com.example.demo.http;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import com.example.demo.constants.ErrorCodeEnum;
import com.example.demo.exception.PaymentProcessingException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class HttpServiceEngine {

	private final RestClient restClient;

	public ResponseEntity<String> makeHttpCall(HttpRequest httpRequest) {

		try {
			ResponseEntity<String> httpResponse = restClient.method(httpRequest.getHttpMethod())
					.uri(httpRequest.getUrl())
					.headers(restClientHeader -> restClientHeader.addAll(httpRequest.getHeaders()))
					.body(httpRequest.getBody()).retrieve().toEntity(String.class);
			log.info("Http response : {} ", httpResponse);

			return httpResponse;
		} catch (HttpClientErrorException | HttpServerErrorException e) {
			if (e.getStatusCode() == HttpStatus.GATEWAY_TIMEOUT
					|| e.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE) {
				log.error("Gateway timeout | paypal service unavailable");

				throw new PaymentProcessingException(ErrorCodeEnum.PAYPAL_PROVIDER_SERVICE_UNAVAILABLE.getErrorCode(),
						ErrorCodeEnum.PAYPAL_PROVIDER_SERVICE_UNAVAILABLE.getErrorMessage(),
						HttpStatus.SERVICE_UNAVAILABLE);

			}

			String errorResponse = e.getResponseBodyAsString();
			return ResponseEntity.status(e.getStatusCode()).body(errorResponse);
		} catch (Exception e) {
			log.error("Error while making http api request from HttpServiceEngine | : {} ", e.getMessage(), e);

			throw new PaymentProcessingException(ErrorCodeEnum.PAYPAL_PROVIDER_SERVICE_UNAVAILABLE.getErrorCode(),
					ErrorCodeEnum.PAYPAL_PROVIDER_SERVICE_UNAVAILABLE.getErrorMessage(),
					HttpStatus.SERVICE_UNAVAILABLE);

		}

	}

}