package org.juniortown.backend.comment.exception;

import org.juniortown.backend.exception.CustomException;
import org.springframework.http.HttpStatus;

public class NoRightForCommentDeleteException extends CustomException {
public static final String MESSAGE = "해당 댓글을 삭제할 권한이 없습니다.";

	public NoRightForCommentDeleteException() {
		super(MESSAGE);
	}

	@Override
	public int getStatusCode() {
		return HttpStatus.FORBIDDEN.value();
	}
}
