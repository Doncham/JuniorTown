package org.juniortown.backend.controller;



import java.util.HashMap;

import org.juniortown.backend.exception.CustomException;
import org.juniortown.backend.exception.PostNotFound;
import org.juniortown.backend.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class ExceptionController {
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ErrorResponse invalidRequestHandler(MethodArgumentNotValidException e) {

		// FieldError fieldError = e.getFieldError();
		// String field = fieldError.getField();
		// String message = fieldError.getDefaultMessage();
		ErrorResponse response = ErrorResponse.builder()
			.code("400")
			.message("잘못된 요청입니다.")
			.build();

		for (FieldError fieldError : e.getFieldErrors()) {
			response.addValidation(fieldError.getField(), fieldError.getDefaultMessage());
		}

		return response;
	}

	//@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(CustomException.class)
	public ResponseEntity<ErrorResponse> customException(CustomException e) {
		int statusCode = e.getStatusCode();
		ErrorResponse body = ErrorResponse.builder()
			.code(String.valueOf(statusCode))
			.message(e.getMessage())
			//.validation(e.getValidation())
			.build();

		ResponseEntity<ErrorResponse> response = ResponseEntity.status(statusCode)
			.body(body);

		return response;
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> exceptionHandler(Exception e) {
		log.error("Exception: ", e);
		ErrorResponse response = ErrorResponse.builder()
			.code("500")
			.message("서버 오류입니다.")
			.build();

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(response);
	}
}
