package com.example.demo.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.pojo.CreateOrderReq;
import com.example.demo.service.interfaces.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/payments")
@Slf4j
@RequiredArgsConstructor
public class PaymentController {

	private final PaymentService paymentService;

	@PostMapping
	public String createPayment(@RequestBody CreateOrderReq createOrderReq) {
		log.info("Create payment || CreateOrderReq : {} ", createOrderReq);

		String response = paymentService.createPayment(createOrderReq);
		log.info("CREATE PAYMENT response : {}  ", response);

		return response;
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
