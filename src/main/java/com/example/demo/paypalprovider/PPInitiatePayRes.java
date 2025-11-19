package com.example.demo.paypalprovider;

import lombok.Data;

@Data
public class PPInitiatePayRes {

	private String orderId;
	private String paymentStatus;
	private String redirectUrl;
}
