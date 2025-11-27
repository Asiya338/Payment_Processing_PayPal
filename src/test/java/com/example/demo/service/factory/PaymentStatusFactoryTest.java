package com.example.demo.service.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import com.example.demo.constants.Constant;
import com.example.demo.service.interfaces.TransactionStatusProcessor;
import com.example.demo.service.statusprocessor.ApprovedStatusProcessor;
import com.example.demo.service.statusprocessor.CreatedStatusProcessor;
import com.example.demo.service.statusprocessor.FailedStatusProcessor;
import com.example.demo.service.statusprocessor.InitiatedStatusProcessor;
import com.example.demo.service.statusprocessor.PendingStatusProcessor;
import com.example.demo.service.statusprocessor.SuccessStatusProcessor;

@ExtendWith(MockitoExtension.class)
class PaymentStatusFactoryTest {

	@Mock
	private ApplicationContext applicationContext;

	@InjectMocks
	private PaymentStatusFactory paymentStatusFactory;

	@Mock
	private CreatedStatusProcessor created;

	@Mock
	private InitiatedStatusProcessor initiated;

	@Mock
	private PendingStatusProcessor pending;

	@Mock
	private ApprovedStatusProcessor approved;

	@Mock
	private SuccessStatusProcessor success;

	@Mock
	private FailedStatusProcessor failed;

	@Test
	void getStatusProccessorTest_created() {

		when(applicationContext.getBean(CreatedStatusProcessor.class)).thenReturn(created);

		TransactionStatusProcessor processor = paymentStatusFactory.getStatusProcessor(Constant.CREATED);

		assertEquals(created, processor);
	}

	@Test
	void getStatusProccessorTest_initiated() {

		when(applicationContext.getBean(InitiatedStatusProcessor.class)).thenReturn(initiated);

		TransactionStatusProcessor processor = paymentStatusFactory.getStatusProcessor(Constant.INTIIATED);

		assertEquals(initiated, processor);
	}

	@Test
	void getStatusProccessorTest_pending() {

		when(applicationContext.getBean(PendingStatusProcessor.class)).thenReturn(pending);

		TransactionStatusProcessor processor = paymentStatusFactory.getStatusProcessor(Constant.PENDING);

		assertEquals(pending, processor);
	}

	@Test
	void getStatusProccessorTest_approved() {

		when(applicationContext.getBean(ApprovedStatusProcessor.class)).thenReturn(approved);

		TransactionStatusProcessor processor = paymentStatusFactory.getStatusProcessor(Constant.APPROVED);

		assertEquals(approved, processor);
	}

	@Test
	void getStatusProccessorTest_success() {
		when(applicationContext.getBean(SuccessStatusProcessor.class)).thenReturn(success);

		TransactionStatusProcessor processor = paymentStatusFactory.getStatusProcessor(5);

		assertEquals(success, processor);
	}

	@Test
	void getStatusProccessorTest_failed() {
		when(applicationContext.getBean(FailedStatusProcessor.class)).thenReturn(failed);

		TransactionStatusProcessor processor = paymentStatusFactory.getStatusProcessor(6);
		assertEquals(failed, processor);
	}

	@Test
	void getStatusProccessorTest_unknown() {
		TransactionStatusProcessor processor = paymentStatusFactory.getStatusProcessor(999);

		assertNull(processor);
	}

}
