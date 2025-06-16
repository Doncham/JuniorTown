package org.juniortown.backend.user.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginResultDTO {
	private String message;
	@Builder
	public LoginResultDTO(String message) {
		this.message = message;
	}
}
