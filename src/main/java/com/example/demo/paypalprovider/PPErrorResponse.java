package com.example.demo.paypalprovider;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PPErrorResponse {
	private String errorCode;
	private String errorMessage;
}
