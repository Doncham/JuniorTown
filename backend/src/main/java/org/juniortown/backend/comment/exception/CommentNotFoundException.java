package org.juniortown.backend.comment.exception;

import org.juniortown.backend.exception.CustomException;

public class CommentNotFoundException extends CustomException {
	public static final String MESSAGE = "해당 댓글을 찾을 수 없습니다.";

	public CommentNotFoundException() {
		super(MESSAGE);
	}

	@Override
	public int getStatusCode() {
		return 404;
	}
}
