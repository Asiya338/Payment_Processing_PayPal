package com.example.demo.paypalprovider;

import lombok.Data;

@Data
public class PPCaptureOrderRes {

	private String orderId;
	private String paymentStatus;
}
