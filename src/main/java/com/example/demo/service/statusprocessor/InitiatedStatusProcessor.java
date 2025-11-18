package com.example.demo.service.statusprocessor;

import org.springframework.stereotype.Service;

import com.example.demo.dto.TransactionDto;
import com.example.demo.service.interfaces.TransactionStatusProcessor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class InitiatedStatusProcessor implements TransactionStatusProcessor {
	@Override

	public TransactionDto processStatus(TransactionDto txnDto) {
		// TODO Auto-generated method stub
		return null;
	}

}