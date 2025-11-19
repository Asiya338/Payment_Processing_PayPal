package com.example.demo.service.impl;

import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.demo.constants.Constant;
import com.example.demo.dao.interfaces.TransactionDao;
import com.example.demo.dto.TransactionDto;
import com.example.demo.entity.TransactionEntity;
import com.example.demo.http.HttpRequest;
import com.example.demo.http.HttpServiceEngine;
import com.example.demo.paypalprovider.PPInitiatePayRes;
import com.example.demo.pojo.CreateOrderReq;
import com.example.demo.pojo.InitiateOrderReq;
import com.example.demo.pojo.PaymentResponse;
import com.example.demo.service.PaymentStatusProcessor;
import com.example.demo.service.PaymentValidator;
import com.example.demo.service.impl.helper.PPCreateOrderHelper;
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
	private final TransactionDao transactionDao;
	private final HttpServiceEngine httpServiceEngine;
	private final PPCreateOrderHelper createOrderHelper;

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
	public PaymentResponse initiatePayment(String txnReference, InitiateOrderReq initiateOrderReq) {
		log.info("Transaction reference || initiatePayment : {} ", txnReference);

		paymentValidator.validateInitiateOrderReq(txnReference, initiateOrderReq);

		TransactionEntity txnEntity = transactionDao.getTransactionByTxnReference(txnReference);
		log.info("Txn entity || initiatePayment : {}  ", txnEntity);

		TransactionDto txnDto = modelMapper.map(txnEntity, TransactionDto.class);
		txnDto.setTxnStatusId(Constant.INTIIATED);

		TransactionDto processedDto = paymentStatusProcessor.processPayment(txnDto);
		log.info("Processes DTO from CREATED to INITIATED : {} ", processedDto);

		HttpRequest httpRequest = createOrderHelper.prepareHttpRequest(processedDto, initiateOrderReq);
		log.info("HTTP REQUEST || initiatePayment : {} ", httpRequest);

		ResponseEntity<String> httpResponse = httpServiceEngine.makeHttpCall(httpRequest);
		log.info("HTTP RESPONSE || httpServiceEngine : {} ", httpResponse);

		PPInitiatePayRes initiateRes = createOrderHelper.prepareResponse(httpResponse);
		log.info("initiateRes : {}  ", initiateRes);

		txnDto.setTxnStatusId(Constant.PENDING);
		txnDto.setProviderReference(initiateRes.getOrderId());
		paymentStatusProcessor.processPayment(txnDto);
		log.info("Processes DTO from INITIATED to PENDING ...");

		PaymentResponse response = new PaymentResponse();
		response.setTxnStatusId(txnDto.getTxnStatusId());
		response.setRedirectUrl(initiateRes.getRedirectUrl());
		response.setProviderReference(initiateRes.getOrderId());
		response.setTxnReference(txnReference);

		return response;
	}

	@Override
	public String capturePayment() {
		// TODO Auto-generated method stub
		return null;
	}

}
