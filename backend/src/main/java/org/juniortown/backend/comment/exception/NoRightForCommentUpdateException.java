package org.juniortown.backend.comment.exception;

import org.juniortown.backend.exception.CustomException;
import org.springframework.http.HttpStatus;

public class NoRightForCommentUpdateException extends CustomException {
public static final String MESSAGE = "해당 댓글을 수정할 권한이 없습니다.";

	public NoRightForCommentUpdateException() {
		super(MESSAGE);
	}

	@Override
	public int getStatusCode() {
		return HttpStatus.FORBIDDEN.value();
	}
}
