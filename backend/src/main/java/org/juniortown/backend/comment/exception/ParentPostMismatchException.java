package org.juniortown.backend.comment.exception;

import org.juniortown.backend.exception.CustomException;

public class ParentPostMismatchException extends CustomException {
	private static final String MESSAGE = "부모 댓글의 게시글과 대댓글이 속한 게시글이 일치하지 않습니다.";

	public ParentPostMismatchException() {
		super(MESSAGE);
	}

	@Override
	public int getStatusCode() {
		return 400; // Bad Request
	}
}
