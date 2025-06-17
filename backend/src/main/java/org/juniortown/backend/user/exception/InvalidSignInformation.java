package org.juniortown.backend.user.exception;

import org.juniortown.backend.exception.CustomException;

public class InvalidSignInformation extends CustomException {
	private static final String MESSAGE = "아이디/비밀번호가 올바르지 않습니다.";

	/****
		 * Constructs an InvalidSignInformation exception with a predefined message indicating incorrect username or password.
		 */
	public InvalidSignInformation() {
		super(MESSAGE);
	}

	@Override
	public int getStatusCode() {
		return 400;
	}
}
