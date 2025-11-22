package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@EnableDiscoveryClient
@OpenAPIDefinition(info = @Info(title = "Payment Processing Service API", version = "1.0", description = "Service"
		+ " logic with core payment status system for processing payments using PayPal APIs"))
@SpringBootApplication
public class PaymentProcessingApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymentProcessingApplication.class, args);
	}

}
