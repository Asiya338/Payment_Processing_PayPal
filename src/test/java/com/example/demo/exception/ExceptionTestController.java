package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
class ExceptionTestController {

	@GetMapping("/processing-error")
	public String throwProcessingError() {
		throw new PaymentProcessingException("ERR100", "PayPal failure", HttpStatus.BAD_REQUEST);
	}

	@GetMapping("/not-found")
	public String throwNotFound() {
		throw new ResourceNotFoundException("ERR404", "Resource missing", HttpStatus.NOT_FOUND);
	}

	@GetMapping("/generic")
	public String throwGeneric() {
		throw new RuntimeException("Something went wrong");
	}
}
