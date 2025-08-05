package org.juniortown.backend.user.exception;

import org.juniortown.backend.exception.CustomException;
import org.springframework.http.HttpStatus;

public class UserNotFoundException extends CustomException {
	public static final String MESSAGE = "해당 사용자를 찾을 수 없습니다.";
	public UserNotFoundException() {
		super(MESSAGE);
	}
	@Override
	public int getStatusCode() {
		return 404;
	}
}
