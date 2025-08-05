package org.juniortown.backend.comment.exception;

import org.juniortown.backend.exception.CustomException;
import org.springframework.http.HttpStatus;

public class CircularReferenceException extends CustomException {
	public static final String MESSAGE = "댓글 순환 참조 발생!";
	public CircularReferenceException() {
		super(MESSAGE);
	}

	@Override
	public int getStatusCode() {
		return HttpStatus.BAD_REQUEST.value();
	}
}
