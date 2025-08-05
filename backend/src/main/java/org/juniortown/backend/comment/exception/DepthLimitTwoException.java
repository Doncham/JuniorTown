package org.juniortown.backend.comment.exception;

import org.juniortown.backend.exception.CustomException;
import org.springframework.http.HttpStatus;

public class DepthLimitTwoException extends CustomException {
	public static final String MESSAGE = "댓글은 최대 2단계까지만 가능합니다.";

	public DepthLimitTwoException() {
		super(MESSAGE);
	}

	@Override
	public int getStatusCode() {
		return HttpStatus.BAD_REQUEST.value();
	}
}
