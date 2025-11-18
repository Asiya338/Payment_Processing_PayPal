package com.example.demo.service.impl;

import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.example.demo.constants.Constant;
import com.example.demo.dto.TransactionDto;
import com.example.demo.pojo.CreateOrderReq;
import com.example.demo.pojo.PaymentResponse;
import com.example.demo.service.PaymentStatusProcessor;
import com.example.demo.service.PaymentValidator;
import com.example.demo.service.interfaces.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

	private final ModelMapper modelMapper;
	private final PaymentStatusProcessor paymentStatusProcessor;
	private final PaymentValidator paymentValidator;

	@Override
	public PaymentResponse createPayment(CreateOrderReq createOrderReq) {
		log.info("Create Order req : {} ", createOrderReq);

		paymentValidator.validateCreateReq(createOrderReq);

		TransactionDto txnDto = modelMapper.map(createOrderReq, TransactionDto.class);
		txnDto.setTxnStatusId(Constant.CREATED);
		txnDto.setTxnReference(UUID.randomUUID().toString());

		log.info("Transaction DTO in createPayment : {} ", txnDto);

		TransactionDto processedDto = paymentStatusProcessor.processPayment(txnDto);
		log.info("processed dto in createPayment : {} ", processedDto);

		PaymentResponse response = new PaymentResponse();
		response.setTxnStatusId(processedDto.getTxnStatusId());
		response.setTxnReference(processedDto.getTxnReference());

		return response;
	}

	@Override
	public String initiatePayment() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String capturePayment() {
		// TODO Auto-generated method stub
		return null;
	}

}
