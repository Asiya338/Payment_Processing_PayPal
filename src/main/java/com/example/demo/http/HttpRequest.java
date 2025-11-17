package com.example.demo.http;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import lombok.Data;

@Data
public class HttpRequest {
	private String url;
	private HttpMethod httpMethod;
	private HttpHeaders headers;
	private Object body;
}
