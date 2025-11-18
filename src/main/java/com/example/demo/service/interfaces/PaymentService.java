package com.example.demo.service.interfaces;

import com.example.demo.pojo.CreateOrderReq;
import com.example.demo.pojo.PaymentResponse;

public interface PaymentService {

	public PaymentResponse createPayment(CreateOrderReq createOrderReq);

	public String initiatePayment();

	public String capturePayment();
}
