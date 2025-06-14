package org.juniortown.backend.user.service;

import java.util.Optional;

import org.juniortown.backend.user.crypto.PasswordEncoder;
import org.juniortown.backend.user.entity.User;
import org.juniortown.backend.user.exception.AlreadyExistsEmailException;
import org.juniortown.backend.user.repository.UserRepository;
import org.juniortown.backend.user.request.SignUpDTO;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
	private final UserRepository userRepository;
	private final BCryptPasswordEncoder encoder;


	public void signUp(SignUpDTO signUpDTO) {
		Boolean isExist = userRepository.existsByEmail(signUpDTO.getEmail());
		if (isExist) {
			throw new AlreadyExistsEmailException();
		}

		String encryptedPassword = encoder.encode(signUpDTO.getPassword());

		User user = User.builder()
			.name(signUpDTO.getName())
			.password(encryptedPassword)
			.email(signUpDTO.getEmail())
			.build();

		userRepository.save(user);
	}
}
