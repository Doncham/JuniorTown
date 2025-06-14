package org.juniortown.backend.user.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SignUpDTO {
	private String name;
	private String password;
	@Email
	private String email;
}
