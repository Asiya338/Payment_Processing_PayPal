package com.example.demo.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.pojo.CreateOrderReq;
import com.example.demo.pojo.InitiateOrderReq;
import com.example.demo.pojo.PaymentResponse;
import com.example.demo.service.interfaces.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/v1/payments")
@Slf4j
@RequiredArgsConstructor
public class PaymentController {

	private final PaymentService paymentService;

	@PostMapping
	public PaymentResponse createPayment(@RequestBody CreateOrderReq createOrderReq) {
		log.info("Create payment || CreateOrderReq : {} ", createOrderReq);

		PaymentResponse response = paymentService.createPayment(createOrderReq);
		log.info("CREATE PAYMENT response : {}  ", response);

		return response;
	}

	@PostMapping("/{txnReference}/initiate")
	public PaymentResponse initiatePayment(@PathVariable String txnReference,
			@RequestBody InitiateOrderReq initiateOrderReq) {
		log.info("Initiate payment || initiateOrderReq : {} ", initiateOrderReq);

		PaymentResponse response = paymentService.initiatePayment(txnReference, initiateOrderReq);
		log.info("INITIATE PAYMENT response : {}  ", response);

		return response;
	}

	@PostMapping("/{txnReference}/capture")
	public PaymentResponse capturePayment(@PathVariable String txnReference) {
		log.info("Capture payment || txnReference : {} ", txnReference);

		PaymentResponse response = paymentService.capturePayment(txnReference);
		log.info("CAPTURE PAYMENT response : {}  ", response);

		return response;
	}

	@GetMapping("/{userId}")
	public List<PaymentResponse> getPaymentsByUserId(@PathVariable String userId) {
		log.info("get all the payment transactions || userId : {} ", userId);

		List<PaymentResponse> response = paymentService.getPaymentsByUserId(userId);
		log.info("All PAYMENTs response : {}  ", response);

		return response;
	}

}
