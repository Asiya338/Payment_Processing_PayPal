package com.example.demo.dao.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.KeyHolder;

import com.example.demo.constants.ErrorCodeEnum;
import com.example.demo.entity.TransactionEntity;
import com.example.demo.exception.PaymentProcessingException;

@ExtendWith(MockitoExtension.class)
class TransactionDaoImplTest {

	@InjectMocks
	private TransactionDaoImpl transactionDaoImpl;

	@Mock
	private NamedParameterJdbcTemplate jdbcTemplate;

	@Test
	void createTransactionTest() {
		TransactionEntity txn = new TransactionEntity();
		txn.setId(110);

		doAnswer(invocation -> {
			KeyHolder kh = invocation.getArgument(2);
			kh.getKeyList().add(Map.of("id", 10));
			return 1;
		}).when(jdbcTemplate).update(any(), any(), any(), any());

		TransactionEntity result = transactionDaoImpl.createTransaction(txn);

		assertEquals(10, result.getId());

	}

	@Test
	void getTransactionByTxnReferenceTest() {
		String txnReference = "TX123";

		TransactionEntity mockEntity = new TransactionEntity();
		mockEntity.setTxnReference(txnReference);
		mockEntity.setUserId(10);

		// Mock queryForObject result
		when(jdbcTemplate.queryForObject(anyString(), anyMap(), any(BeanPropertyRowMapper.class)))
				.thenReturn(mockEntity);

		// Act
		TransactionEntity result = transactionDaoImpl.getTransactionByTxnReference(txnReference);

		// Assert
		assertNotNull(result);
		assertEquals("TX123", result.getTxnReference());
		assertEquals(10, result.getUserId());
	}

	@Test
	void testUpdateTransaction_success() {

		TransactionEntity txn = new TransactionEntity();
		txn.setId(1);
		txn.setTxnStatusId(2);
		txn.setProviderReference("PR123");
		txn.setErrorCode(null);
		txn.setErrorMessage(null);

		// When JDBC update is called, return 1 row updated
		when(jdbcTemplate.update(anyString(), anyMap())).thenReturn(1);

		TransactionEntity result = transactionDaoImpl.updateTransaction(txn);

		assertEquals(1, result.getId());
		assertEquals(2, result.getTxnStatusId());
		assertEquals("PR123", result.getProviderReference());
	}

	@Test
	void testUpdateTransaction_failed() {

		TransactionEntity txn = new TransactionEntity();
		txn.setId(1);

		// Return 0 rows updated → DAO should throw exception
		when(jdbcTemplate.update(anyString(), anyMap())).thenReturn(0);

		PaymentProcessingException ex = assertThrows(PaymentProcessingException.class,
				() -> transactionDaoImpl.updateTransaction(txn));

		assertEquals(ErrorCodeEnum.ERROR_UPDATING_TRANSACTION.getErrorCode(), ex.getErrorCode());
	}

	@Test
	void testGetTransactionByUserId_success() {

		String userId = "10";

		TransactionEntity txn1 = new TransactionEntity();
		txn1.setId(1);
		txn1.setUserId(10);

		TransactionEntity txn2 = new TransactionEntity();
		txn2.setId(2);
		txn2.setUserId(10);

		List<TransactionEntity> mockList = List.of(txn1, txn2);

		// Mock JDBC query() to return list
		when(jdbcTemplate.query(anyString(), anyMap(), any(BeanPropertyRowMapper.class))).thenReturn(mockList);

		List<TransactionEntity> result = transactionDaoImpl.getTransactionsByUserId(userId);

		assertEquals(2, result.size());
		assertEquals(1, result.get(0).getId());
		assertEquals(2, result.get(1).getId());
	}

}
