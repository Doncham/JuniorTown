package org.juniortown.backend.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class LoginDTO {
	@NotBlank(message = "이메일을 입력해주세요.")
	@Email(message = "유효한 이메일 주소여야 합니다.")
	private String email;

	@NotBlank(message = "비밀번호를 입력해주세요.")
	//@Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
	private String password;
	/**
		 * Constructs a LoginDTO with the specified email and password.
		 *
		 * @param email the user's email address
		 * @param password the user's password
		 */
	@Builder
	public LoginDTO(String email, String password) {
		this.email = email;
		this.password = password;
	}
}
