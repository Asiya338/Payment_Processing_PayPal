package com.example.demo.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/payments")
@Slf4j
public class PaymentController {

	@PostMapping
	public String createPayment() {
		return null;
	}

	@PostMapping("/{orderId}/initiate")
	public String initiatePayment(@PathVariable String orderId) {
		return null;
	}

	@PostMapping("/{orderId}/capture")
	public String capturePayment(@PathVariable String orderId) {
		return null;
	}

}
