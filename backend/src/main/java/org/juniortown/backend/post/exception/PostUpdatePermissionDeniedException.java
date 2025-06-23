package org.juniortown.backend.post.exception;

import org.juniortown.backend.exception.CustomException;
import org.springframework.http.HttpStatus;

public class PostUpdatePermissionDeniedException extends CustomException {
	private static final String MESSAGE = "해당 게시글을 수정할 권한이 없습니다.";

	public PostUpdatePermissionDeniedException() {
		super(MESSAGE);
	}

	@Override
	public int getStatusCode() {
		return HttpStatus.FORBIDDEN.value();
	}
}
