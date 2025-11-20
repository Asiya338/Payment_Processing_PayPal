package com.example.demo.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.demo.constants.Constant;
import com.example.demo.constants.ErrorCodeEnum;
import com.example.demo.dao.interfaces.TransactionDao;
import com.example.demo.dto.TransactionDto;
import com.example.demo.entity.TransactionEntity;
import com.example.demo.exception.PaymentProcessingException;
import com.example.demo.http.HttpRequest;
import com.example.demo.http.HttpServiceEngine;
import com.example.demo.paypalprovider.PPCaptureOrderRes;
import com.example.demo.paypalprovider.PPInitiatePayRes;
import com.example.demo.pojo.CreateOrderReq;
import com.example.demo.pojo.InitiateOrderReq;
import com.example.demo.pojo.PaymentResponse;
import com.example.demo.service.PaymentStatusProcessor;
import com.example.demo.service.PaymentValidator;
import com.example.demo.service.impl.helper.PPCaptureOrderHelper;
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
	private final PPCaptureOrderHelper ppCaptureOrderHelper;

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

		try {
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
		} catch (PaymentProcessingException ex) {
			log.error("Error while initiating payment : {} ", ex.getMessage(), ex);

			txnDto.setTxnStatusId(Constant.FAILED);
			txnDto.setErrorCode(ex.getErrorCode());
			txnDto.setErrorMessage(ex.getErrorMessage());

			TransactionDto failedDto = paymentStatusProcessor.processPayment(txnDto);
			log.info("Processes DTO from .. to FALIED || initiatePayment : {}", failedDto);

			throw ex;

		} catch (Exception ex) {
			log.error("Unexpected Error while initiating payment : {} ", ex.getMessage(), ex);

			txnDto.setTxnStatusId(Constant.FAILED);
			txnDto.setErrorCode(ErrorCodeEnum.UNEXPECTED_ERROR.getErrorCode());
			txnDto.setErrorMessage(ErrorCodeEnum.UNEXPECTED_ERROR.getErrorMessage());

			TransactionDto failedDto = paymentStatusProcessor.processPayment(txnDto);
			log.info("Processes DTO from .. to FALIED ||initiatePayment : {}", failedDto);

			throw ex;
		}

	}

	@Override
	public PaymentResponse capturePayment(String txnReference) {
		log.info(" txnReference || capturePayment : {}  ", txnReference);

		TransactionEntity txnEntity = transactionDao.getTransactionByTxnReference(txnReference);
		log.info("TransactionEntity for given txnReference : {} | : {}  ", txnReference, txnEntity);

		TransactionDto txnDto = modelMapper.map(txnEntity, TransactionDto.class);
		log.info("TransactionDto || capturePayment : {} ", txnDto);

		txnDto.setTxnStatusId(Constant.APPROVED);
		paymentStatusProcessor.processPayment(txnDto);

		try {
			HttpRequest httpRequest = ppCaptureOrderHelper.prepareHttpRequest(txnDto);
			log.info("HttpRequest || capturePayment : {} ", httpRequest);

			ResponseEntity<String> httpResponse = httpServiceEngine.makeHttpCall(httpRequest);
			log.info("Http Response || capturePayment : {} ", httpResponse);

			PPCaptureOrderRes captureResponse = ppCaptureOrderHelper.prepareHttpResponse(httpResponse);
			log.info(" captureResponse || capture payment : {} ", captureResponse);

			txnDto.setTxnStatusId(Constant.SUCCESS);
			paymentStatusProcessor.processPayment(txnDto);
			log.info("Updated payment status to SUCCESS... ");

			PaymentResponse response = new PaymentResponse();
			response.setTxnStatusId(txnDto.getTxnStatusId());
			response.setTxnReference(txnDto.getTxnReference());
			response.setProviderReference(captureResponse.getOrderId());

			log.info("Cature payment response : {}  ", response);

			return response;
		} catch (PaymentProcessingException ex) {
			log.error("Received error response || capture payment : {} ", ex.getErrorMessage(), ex);

			/*
			 * Here we don't update it as FAILED, as it has been approved by user, then
			 * money will be debitd from it's account. so we can build reconcilation sytsem
			 * that will check handle pending approved payments
			 * 
			 */
			throw ex;
		} catch (Exception ex) {
			log.error("Received Unexpected error response || capture payment : {} ", ex.getMessage(), ex);

			/*
			 * Here we don't update it as FAILED, as it has been approved by user, then
			 * money will be debitd from it's account. so we can build reconcilation sytsem
			 * that will check handle pending approved payments
			 * 
			 */
			throw ex;
		}
	}

	@Override
	public List<PaymentResponse> getPaymentsByUserId(String userId) {
		log.info("getPaymentsByUserId || userId : {} ", userId);

		List<TransactionEntity> transactions = transactionDao.getTransactionsByUserId(userId);
		List<PaymentResponse> response = new ArrayList<>();
		for (TransactionEntity transaction : transactions) {
			response.add(modelMapper.map(transaction, PaymentResponse.class));
		}

		log.info("ALL PAYMENT TRANSACTIONS  : {} ", response);

		return response;
	}

}
