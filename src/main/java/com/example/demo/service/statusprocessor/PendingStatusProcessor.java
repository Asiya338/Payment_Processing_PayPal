package com.example.demo.service.statusprocessor;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.example.demo.dao.interfaces.TransactionDao;
import com.example.demo.dto.TransactionDto;
import com.example.demo.entity.TransactionEntity;
import com.example.demo.service.interfaces.TransactionStatusProcessor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PendingStatusProcessor implements TransactionStatusProcessor {

	private final ModelMapper modelMapper;
	private final TransactionDao transactionDao;

	@Override
	public TransactionDto processStatus(TransactionDto txnDto) {
		log.info("Transaction Dto || PendingStatusProcessor : {}  ", txnDto);

		TransactionEntity txnEntity = modelMapper.map(txnDto, TransactionEntity.class);
		log.info("Transaction Entity || PendingStatusProcessor : {} ", txnEntity);

		TransactionEntity updatedEntity = transactionDao.updateTransaction(txnEntity);
		log.info("Updated Transaction Entity || PendingStatusProcessor : {}", updatedEntity);

		return txnDto;
	}

}