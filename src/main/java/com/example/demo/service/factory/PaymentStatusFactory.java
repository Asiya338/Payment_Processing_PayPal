package com.example.demo.service.factory;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.example.demo.service.interfaces.TransactionStatusProcessor;
import com.example.demo.service.statusprocessor.ApprovedStatusProcessor;
import com.example.demo.service.statusprocessor.CreatedStatusProcessor;
import com.example.demo.service.statusprocessor.FailedStatusProcessor;
import com.example.demo.service.statusprocessor.InitiatedStatusProcessor;
import com.example.demo.service.statusprocessor.PendingStatusProcessor;
import com.example.demo.service.statusprocessor.SuccessStatusProcessor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentStatusFactory {

	private final ApplicationContext appContext;

	public TransactionStatusProcessor getStatusProcessor(int statusId) {

		switch (statusId) {
		case 1:
			log.info("Created status processor  ");
			return appContext.getBean(CreatedStatusProcessor.class);

		case 2:
			log.info("Initiated status processor  ");
			return appContext.getBean(InitiatedStatusProcessor.class);

		case 3:
			log.info("Pending status processor  ");
			return appContext.getBean(PendingStatusProcessor.class);

		case 4:
			log.info("Approved status processor  ");
			return appContext.getBean(ApprovedStatusProcessor.class);

		case 5:
			log.info("Success status processor  ");
			return appContext.getBean(SuccessStatusProcessor.class);

		case 6:
			log.info("Failed status processor  ");
			return appContext.getBean(FailedStatusProcessor.class);

		default:
			log.info("NO status processor found ...");
			return null;
		}
	}

}
