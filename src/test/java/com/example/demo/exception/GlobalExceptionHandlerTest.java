package com.example.demo.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.constants.ErrorCodeEnum;

@WebMvcTest(controllers = ExceptionTestController.class)
@Import(GlobalExceptionHandler.class)
class GlobalExceptionHandlerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void testProcessingServiceException() throws Exception {
		mockMvc.perform(get("/test/processing-error")).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errorCode").value("ERR100"))
				.andExpect(jsonPath("$.errorMessage").value("PayPal failure"));
	}

	@Test
	void testResourceNotFoundException() throws Exception {
		mockMvc.perform(get("/test/not-found")).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.errorCode").value("ERR404"))
				.andExpect(jsonPath("$.errorMessage").value("Resource missing"));
	}

	@Test
	void testGenericException() throws Exception {
		mockMvc.perform(get("/test/generic")).andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.errorCode").value(ErrorCodeEnum.GENERIC_ERROR.getErrorCode()))
				.andExpect(jsonPath("$.errorMessage").value(ErrorCodeEnum.GENERIC_ERROR.getErrorMessage()));
	}
}
