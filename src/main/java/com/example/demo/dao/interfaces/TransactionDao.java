package com.example.demo.dao.interfaces;

import com.example.demo.entity.TransactionEntity;

public interface TransactionDao {

	public TransactionEntity createTransaction(TransactionEntity txnEntity);

	public TransactionEntity getTransactionByTxnReference(String txnReference);

	public TransactionEntity updateTransaction(TransactionEntity txnEntity);
}
