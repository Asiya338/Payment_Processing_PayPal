package com.example.demo.service.impl.helper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.demo.constants.Constant;
import com.example.demo.constants.ErrorCodeEnum;
import com.example.demo.dto.TransactionDto;
import com.example.demo.exception.PaymentProcessingException;
import com.example.demo.http.HttpRequest;
import com.example.demo.paypalprovider.PPCaptureOrderRes;
import com.example.demo.paypalprovider.PPErrorResponse;
import com.example.demo.utils.JsonUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PPCaptureOrderHelper {

	@Value("${paypalprovider.captureodrer.url}")
	private String paypalCaptureOrderUrl;

	private final JsonUtil jsonUtil;

	public HttpRequest prepareHttpRequest(TransactionDto txnDto) {
		log.info("Transaction DTO || PPCaptureOrderHelper : {} ", txnDto);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpRequest httpRequest = new HttpRequest();
		httpRequest.setUrl(paypalCaptureOrderUrl.replace(Constant.ORDER_ID, txnDto.getProviderReference()));
		httpRequest.setHttpMethod(HttpMethod.POST);
		httpRequest.setHeaders(headers);
		httpRequest.setBody(Constant.NULL_BODY);

		log.info("prepared HTTP Request for capture order : {} ", httpRequest);

		return httpRequest;
	}

	public PPCaptureOrderRes prepareHttpResponse(ResponseEntity<String> httpResponse) {
		log.info(" httpResponse || PPCaptureOrderHelper : {} ", httpResponse);

		if (httpResponse.getStatusCode().is2xxSuccessful()) {
			log.info("got successfull response : {} ", httpResponse);

			PPCaptureOrderRes ppCaptureOrderRes = jsonUtil.fromJson(httpResponse.getBody(), PPCaptureOrderRes.class);

			if (ppCaptureOrderRes != null && ppCaptureOrderRes.getOrderId() != null
					&& ppCaptureOrderRes.getPaymentStatus().equals(Constant.COMPLETED)) {

				log.info("got valid response || PPCaptureOrderRes : {} ", ppCaptureOrderRes);

				return ppCaptureOrderRes;
			}

			log.error("Received invalid response || PPCaptureOrderRes : {} ", ppCaptureOrderRes);
		}

		else if (httpResponse.getStatusCode().is4xxClientError() || httpResponse.getStatusCode().is5xxServerError()) {
			log.error("Received 4xx, 5xx error : {} ", httpResponse);

			PPErrorResponse errorResponse = jsonUtil.fromJson(httpResponse.getBody(), PPErrorResponse.class);
			log.error("PPErrorResponse || capture payment : {} ", errorResponse);

			throw new PaymentProcessingException(errorResponse.getErrorCode(), errorResponse.getErrorMessage(),
					HttpStatus.valueOf(httpResponse.getStatusCode().value()));
		}

		log.error("Unknown error occured while captring paument : {} ", httpResponse.getBody());

		throw new PaymentProcessingException(ErrorCodeEnum.PAYPAL_PROVIDER_UNKNOWN_ERROR.getErrorCode(),
				ErrorCodeEnum.PAYPAL_PROVIDER_UNKNOWN_ERROR.getErrorMessage(), HttpStatus.INTERNAL_SERVER_ERROR);

	}

}
