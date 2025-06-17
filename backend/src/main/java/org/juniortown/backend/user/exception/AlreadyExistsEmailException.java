package org.juniortown.backend.user.exception;

import org.juniortown.backend.exception.CustomException;

public class AlreadyExistsEmailException extends CustomException {
	public static String MESSAGE = "이미 가입된 이메일입니다.";
	/****
		 * Constructs an exception indicating that the email address is already registered.
		 */
	public AlreadyExistsEmailException() {
		super(MESSAGE);
	}

	@Override
	public int getStatusCode() {
		return 400;
	}
}
