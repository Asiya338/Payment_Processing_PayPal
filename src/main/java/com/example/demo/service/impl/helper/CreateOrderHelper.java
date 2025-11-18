package com.example.demo.service.impl.helper;

import org.springframework.stereotype.Service;

import com.example.demo.http.HttpRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CreateOrderHelper {

	public HttpRequest prepareHttpRequest() {

		HttpRequest httpRequest = new HttpRequest();

		return httpRequest;
	}
}
