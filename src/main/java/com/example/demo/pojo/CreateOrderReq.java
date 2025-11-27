package com.example.demo.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class CreateOrderReq {
	private int userId;
	private String merchantTransactionReference;
	private int paymentMethodId;
	private int paymentTypeId;
	private int providerId;
	private double amount;
	private String currency;
}
