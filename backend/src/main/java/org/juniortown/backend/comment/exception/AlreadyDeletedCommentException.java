package org.juniortown.backend.comment.exception;

import org.juniortown.backend.exception.CustomException;
import org.springframework.http.HttpStatus;

public class AlreadyDeletedCommentException extends CustomException {
	public static final String MESSAGE = "이미 삭제된 댓글입니다.";

	public AlreadyDeletedCommentException() {
		super(MESSAGE);
	}

	@Override
	public int getStatusCode() {
		return HttpStatus.NOT_FOUND.value();
	}
}
