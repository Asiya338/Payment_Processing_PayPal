package com.example.demo.service.interfaces;

import com.example.demo.pojo.CreateOrderReq;

public interface PaymentService {

	public String createPayment(CreateOrderReq createOrderReq);

	public String initiatePayment();

	public String capturePayment();
}
