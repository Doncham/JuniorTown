package org.juniortown.backend.exception;

import lombok.Getter;

@Getter
public class InvalidRequest extends CustomException{
	private static final String MESSAGE = "잘못된 요청입니다.";
	public String fieldName;
	public String message;
	public InvalidRequest() {
		super(MESSAGE);
	}
	public InvalidRequest(String fieldName, String message) {
		super(MESSAGE);
		this.fieldName = fieldName;
		this.message = message;
		addValidation(fieldName, message);
	}

	@Override
	public int getStatusCode() {
		return 400;
	}
}
