package org.juniortown.backend.user.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SignUpDTO {
	@NotBlank(message = "이름을 입력해주세요.")
	@Size(min = 2, max = 20, message = "이름은 2자 이상 20자 이하로 입력해주세요.")
	private String name;

	// @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
	// 로컬에서는 일단 주석
	@NotBlank(message = "비밀번호를 입력해주세요.")
	private String password;

	@NotBlank(message = "이메일을 입력해주세요.")
	@Email(message = "유효한 이메일 형식이어야 합니다.")
	private String email;
}
