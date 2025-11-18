package com.example.demo.paypalprovider;

import lombok.Data;

@Data
public class PPCreateOrderReq {

	private double amount;
	private String currencyCode;
	private String returnurl;
	private String cancelUrl;
}
