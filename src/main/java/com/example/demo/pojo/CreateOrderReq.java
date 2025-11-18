package com.example.demo.pojo;

import lombok.Data;

@Data
public class CreateOrderReq {
	private String userId;
	private String merchantTransactionReference;
	private int paymentMethodId;
	private int paymentTypeId;
	private int providerId;
	private double amount;
	private String currency;
}
