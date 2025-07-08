package org.juniortown.backend.like.exception;

import org.juniortown.backend.exception.CustomException;

import lombok.Getter;


public class LikeFailureException extends CustomException {
	public static final String MESSAGE = "게시글이 삭제되었거나, 좋아요/좋아요 취소 처리 실패";
	public LikeFailureException(Exception e) {
		super(MESSAGE, e);
	}
	@Override
	public int getStatusCode() {
		return 500;
	}
}
