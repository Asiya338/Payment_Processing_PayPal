package com.example.demo.service.interfaces;

import com.example.demo.dto.TransactionDto;

public interface TransactionStatusProcessor {
	public TransactionDto processStatus(TransactionDto txnDto);
}
