package com.example.demo.utils;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.example.demo.constants.ErrorCodeEnum;
import com.example.demo.exception.PaymentProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class JsonUtil {

	private final ObjectMapper objectMapper;

	public String toJson(Object obj) {
		try {
			return objectMapper.writeValueAsString(obj);
		} catch (Exception e) {
			log.error("Error converting to json : {} ", e.getMessage(), e);

			throw new PaymentProcessingException(ErrorCodeEnum.TO_JSON_ERROR.getErrorCode(),
					ErrorCodeEnum.TO_JSON_ERROR.getErrorMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	public <T> T fromJson(String json, Class<T> clazz) {
		try {
			return objectMapper.readValue(json, clazz);
		} catch (Exception e) {
			log.error("Error converting from json to : {} ", e.getMessage(), e);

			throw new PaymentProcessingException(ErrorCodeEnum.FROM_JSON_ERROR.getErrorCode(),
					ErrorCodeEnum.FROM_JSON_ERROR.getErrorMessage(), HttpStatus.BAD_REQUEST);
		}
	}
}
