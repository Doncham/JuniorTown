package org.juniortown.backend.post.exception;

import org.juniortown.backend.exception.CustomException;

public class PostNotFoundException extends CustomException {
	private static final String MESSAGE = "해당 게시글을 찾을 수 없습니다.";
	public PostNotFoundException() {
		super(MESSAGE);
	}

	@Override
	public int getStatusCode() {
		return 404;
	}
}
