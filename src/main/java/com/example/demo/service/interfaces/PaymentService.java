package com.example.demo.service.interfaces;

import java.util.List;

import com.example.demo.pojo.CreateOrderReq;
import com.example.demo.pojo.InitiateOrderReq;
import com.example.demo.pojo.PaymentResponse;

public interface PaymentService {

	public PaymentResponse createPayment(CreateOrderReq createOrderReq);

	public PaymentResponse initiatePayment(String txnReference, InitiateOrderReq initiateOrderReq);

	public PaymentResponse capturePayment(String txnReference);

	public List<PaymentResponse> getPaymentsByUserId(String userId);
}
