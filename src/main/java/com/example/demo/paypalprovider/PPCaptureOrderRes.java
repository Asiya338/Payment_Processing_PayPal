package com.example.demo.paypalprovider;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PPCaptureOrderRes {

	private String orderId;
	private String paymentStatus;
}
