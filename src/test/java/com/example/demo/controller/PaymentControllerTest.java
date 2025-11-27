package com.example.demo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.exception.GlobalExceptionHandler;
import com.example.demo.pojo.CreateOrderReq;
import com.example.demo.pojo.InitiateOrderReq;
import com.example.demo.pojo.PaymentResponse;
import com.example.demo.service.interfaces.PaymentService;

@WebMvcTest(PaymentController.class)
@Import(GlobalExceptionHandler.class) // if using global exception handler
class PaymentControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private PaymentService paymentService;

	@Test
	void testCreatePayment() throws Exception {

		CreateOrderReq req = new CreateOrderReq(6776221, "ORD_9090_1897_1121", 1, 1, 96, 1, "USD");
		PaymentResponse response = new PaymentResponse("TX1234567890123456789012345678901234", 1, null, null);

		when(paymentService.createPayment(any(CreateOrderReq.class))).thenReturn(response);

		mockMvc.perform(
				post("/v1/payments").contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8").content("""
						{
						  "userId": 6776221,
						  "merchantTransactionReference": "ORD_9090_1897_1121",
						  "paymentMethodId": 1,
						  "paymentTypeId": 1,
						  "providerId": 1,
						  "amount": 96,
						  "currency": "USD"
						}
						""")).andExpect(status().isOk())
				.andExpect(jsonPath("$.txnReference").value("TX1234567890123456789012345678901234"))
				.andExpect(jsonPath("$.txnStatusId").value(1));

	}

	@Test
	void testInitiatePayment() throws Exception {

		InitiateOrderReq req = new InitiateOrderReq("https://merchant-app.com/payment/success",
				"https://merchant-app.com/payment/cancel");

		PaymentResponse response = new PaymentResponse("TX1234567890123456789012345678901234", 3, "redirect-url",
				"providerRef123");

		when(paymentService.initiatePayment(anyString(), any(InitiateOrderReq.class))).thenReturn(response);

		mockMvc.perform(post("/v1/payments/{txnReference}/initiate", "TX1234567890123456789012345678901234")
				.contentType(MediaType.APPLICATION_JSON).content("""
						{
						    "successUrl": "https://merchant-app.com/payment/success",
						    "cancelUrl": "https://merchant-app.com/payment/cancel"
						}
						""")).andExpect(status().isOk())
				.andExpect(jsonPath("$.txnReference").value("TX1234567890123456789012345678901234"))
				.andExpect(jsonPath("$.txnStatusId").value(3))
				.andExpect(jsonPath("$.providerReference").value("providerRef123"))
				.andExpect(jsonPath("$.redirectUrl").value("redirect-url"));

	}

	@Test
	void testCapturePayment() throws Exception {

		PaymentResponse response = new PaymentResponse("TX1234567890123456789012345678901234", 5, null, null);

		when(paymentService.capturePayment(anyString())).thenReturn(response);

		mockMvc.perform(post("/v1/payments/{txnReference}/capture", "TX1234567890123456789012345678901234")
				.contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8")).andExpect(status().isOk())
				.andExpect(jsonPath("$.txnReference").value("TX1234567890123456789012345678901234"))
				.andExpect(jsonPath("$.txnStatusId").value(5));
	}

	@Test
	void testGetPayments() throws Exception {

		// Arrange: Create mock payment responses
		PaymentResponse p1 = new PaymentResponse("TXN001", 1, "redirect1", "ref001");
		PaymentResponse p2 = new PaymentResponse("TXN002", 2, "redirect2", "ref002");

		List<PaymentResponse> responseList = List.of(p1, p2);

		// Mock the service response
		when(paymentService.getPaymentsByUserId("user123")).thenReturn(responseList);

		// Act & Assert
		mockMvc.perform(get("/v1/payments/{userId}", "user123")).andExpect(status().isOk())
				// Validate JSON array size
				.andExpect(jsonPath("$.size()").value(2))

				// Validate first payment response fields
				.andExpect(jsonPath("$[0].txnReference").value("TXN001"))
				.andExpect(jsonPath("$[0].txnStatusId").value(1))
				.andExpect(jsonPath("$[0].redirectUrl").value("redirect1"))
				.andExpect(jsonPath("$[0].providerReference").value("ref001"))

				// Validate second payment response fields
				.andExpect(jsonPath("$[1].txnReference").value("TXN002"))
				.andExpect(jsonPath("$[1].txnStatusId").value(2))
				.andExpect(jsonPath("$[1].redirectUrl").value("redirect2"))
				.andExpect(jsonPath("$[1].providerReference").value("ref002"));
	}

}
