package com.example.demo.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.example.demo.constants.ErrorCodeEnum;
import com.example.demo.dto.TransactionDto;
import com.example.demo.exception.PaymentProcessingException;
import com.example.demo.service.factory.PaymentStatusFactory;
import com.example.demo.service.interfaces.TransactionStatusProcessor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentStatusProcessor {

	private final PaymentStatusFactory paymentStatusFactory;

	public TransactionDto processPayment(TransactionDto txnDto) {
		log.info("Transaction Dto in PaymentStatusProcessor : {} ", txnDto);

		int txnId = txnDto.getTxnStatusId();
		TransactionStatusProcessor processor = paymentStatusFactory.getStatusProcessor(txnId);

		if (processor == null) {
			log.info("NO status processor found for txn ID : {}   ", txnId);

			throw new PaymentProcessingException(ErrorCodeEnum.NO_STATUS_PROCESSOR_FOUND.getErrorCode(),
					ErrorCodeEnum.NO_STATUS_PROCESSOR_FOUND.getErrorMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

		TransactionDto response = processor.processStatus(txnDto);
		log.info("Transaction DTO after processing status  : {} ", response);

		return response;
	}
}
