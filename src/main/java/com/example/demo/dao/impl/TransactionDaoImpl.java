package com.example.demo.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.example.demo.constants.ErrorCodeEnum;
import com.example.demo.dao.interfaces.TransactionDao;
import com.example.demo.entity.TransactionEntity;
import com.example.demo.exception.PaymentProcessingException;

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
		log.info("Getting Transaction by txn reference : {} ", txnReference);

		String sql = "SELECT * FROM `transaction` WHERE txnReference = :txnReference LIMIT 1";

		Map<String, Object> params = new HashMap<>();
		params.put("txnReference", txnReference);

		TransactionEntity txnEntity = jdbcTemplate.queryForObject(sql, params,
				new BeanPropertyRowMapper<>(TransactionEntity.class));
		log.info("Txn entity fetched by txnReference : {} ", txnEntity);

		return txnEntity;
	}

	@Override
	public TransactionEntity updateTransaction(TransactionEntity txnEntity) {
		log.info("Txn entity || updateTransaction : {} ", txnEntity);

		String sql = "UPDATE `transaction` SET txnStatusId = :txnStatusId,"
				+ " providerRefernce = :providerReference , errorCode = :errorCode ,"
				+ "errorMessage = :errorMessage WHERE id = :id LIMIT 1 ";

		Map<String, Object> params = new HashMap<>();
		params.put("txnStatusId", txnEntity.getTxnStatusId());
		params.put("providerReference", txnEntity.getProviderReference());
		params.put("id", txnEntity.getId());
		params.put("errorCode", txnEntity.getErrorCode());
		params.put("errorMessage", txnEntity.getErrorMessage());

		int effectedRows = jdbcTemplate.update(sql, params);

		if (effectedRows == 0) {
			log.info("No transaction found for given id, failed to update txn... ");

			throw new PaymentProcessingException(ErrorCodeEnum.ERROR_UPDATING_TRANSACTION.getErrorCode(),
					ErrorCodeEnum.ERROR_UPDATING_TRANSACTION.getErrorMessage(), HttpStatus.BAD_REQUEST);
		}

		return txnEntity;
	}

	@Override
	public List<TransactionEntity> getTransactionsByUserId(String userId) {
		log.info("User Id || TransactionDaoImpl : {}  ", userId);

		String sql = "SELECT * FROM  `transaction` WHERE userId = :userId ";

		Map<String, Object> params = new HashMap<>();
		params.put("userId", userId);

		List<TransactionEntity> transactions = jdbcTemplate.query(sql, params,
				new BeanPropertyRowMapper(TransactionEntity.class));
		log.info("All transaction done by user with userId : {} || : {} ", userId, transactions);

		return transactions;
	}

}
