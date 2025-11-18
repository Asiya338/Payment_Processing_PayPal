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
public class CreatedStatusProcessor implements TransactionStatusProcessor {

	private final ModelMapper modelMapper;
	private final TransactionDao transactionDao;

	@Override
	public TransactionDto processStatus(TransactionDto txnDto) {

		log.info("Transaction DTO || CreatedStatusProcessor : {} ", txnDto);

		TransactionEntity txnEntity = modelMapper.map(txnDto, TransactionEntity.class);
		log.info("Txn ENTITY  || CreatedStatusProcessor : {} ", txnEntity);

		TransactionEntity responseEntity = transactionDao.createTransaction(txnEntity);
		log.info("response ENTITY  || CreatedStatusProcessor : {}  ", responseEntity);

		txnDto.setId(responseEntity.getId());
		log.info("Transaction CREATED with ID : {} ", txnDto.getId());

		return txnDto;
	}

}
