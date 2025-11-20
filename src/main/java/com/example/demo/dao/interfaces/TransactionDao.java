package com.example.demo.dao.interfaces;

import java.util.List;

import com.example.demo.entity.TransactionEntity;

public interface TransactionDao {

	public TransactionEntity createTransaction(TransactionEntity txnEntity);

	public TransactionEntity getTransactionByTxnReference(String txnReference);

	public TransactionEntity updateTransaction(TransactionEntity txnEntity);

	public List<TransactionEntity> getTransactionsByUserId(String userId);
}
