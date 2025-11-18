package com.example.demo.dao.impl;

import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.example.demo.dao.interfaces.TransactionDao;
import com.example.demo.entity.TransactionEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
@RequiredArgsConstructor
public class TransactionDaoImpl implements TransactionDao {

	private final NamedParameterJdbcTemplate jdbcTemplate;

	@Override
	public TransactionEntity createTransaction(TransactionEntity txnEntity) {
		log.info("TransactionEntity || createTransaction : {} ", txnEntity);

		String sql = "INSERT INTO `transaction` (userId , paymentMethodId , providerId  "
				+ "paymentTypeId , txnStatusId , amount , currency , merchantTransactionReference,"
				+ "txnReference , providerReference , retryCount , errorCode , errorMessage) VALUES "
				+ "(:userId , :paymentMethodId , :providerId , :paymentTypeId , :txnStatusId , :amount ,"
				+ " :currency , :merchantTransactionReference, :txnReference , :providerReference , "
				+ ":retryCount , :errorCode , :errorMessage)";

		BeanPropertySqlParameterSource params = new BeanPropertySqlParameterSource(txnEntity);

		KeyHolder keyHolder = new GeneratedKeyHolder();

		jdbcTemplate.update(sql, params, keyHolder, new String[] { "id" });

		Number generatedKey = keyHolder.getKey();
		if (generatedKey != null) {
			txnEntity.setId(generatedKey.intValue());
		}

		log.info("Transaction Entity created in table with ID : {} || txn entity ", txnEntity.getId(), txnEntity);

		return txnEntity;
	}

	@Override
	public TransactionEntity getTransactionByTxnReference(String txnReference) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TransactionEntity updateTransaction(TransactionEntity txnEntity) {
		// TODO logic
		return txnEntity;
	}
}
