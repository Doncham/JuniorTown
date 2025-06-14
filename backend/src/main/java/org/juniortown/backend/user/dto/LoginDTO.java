package org.juniortown.backend.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class LoginDTO {
	private String email;
	private String password;
}
