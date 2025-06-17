package org.juniortown.backend.user.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginResultDTO {
	private String message;
	/**
	 * Constructs a LoginResultDTO with the specified login result message.
	 *
	 * @param message the message describing the result of a login attempt
	 */
	@Builder
	public LoginResultDTO(String message) {
		this.message = message;
	}
}
