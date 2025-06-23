package org.juniortown.backend.post.exception;

import org.juniortown.backend.exception.CustomException;
import org.springframework.http.HttpStatus;

public class PostDeletePermissionDeniedException extends CustomException {
	private static final String MESSAGE = "해당 게시글을 삭제할 권한이 없습니다.";

	public PostDeletePermissionDeniedException() {
		super(MESSAGE);
	}

	@Override
	public int getStatusCode() {
		// 403
		return HttpStatus.FORBIDDEN.value();
	}
}
