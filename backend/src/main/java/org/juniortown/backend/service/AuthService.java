package org.juniortown.backend.service;

import java.util.Optional;

import org.juniortown.backend.crypto.PasswordEncoder;
import org.juniortown.backend.domain.Session;
import org.juniortown.backend.domain.User;
import org.juniortown.backend.exception.AlreadyExistsEmailException;
import org.juniortown.backend.exception.InvalidSignInformation;
import org.juniortown.backend.repository.UserRepository;
import org.juniortown.backend.request.Login;
import org.juniortown.backend.request.Signup;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
	private final UserRepository userRepository;

	@Transactional
	public Long signIn(Login login) {
		User user = userRepository.findByEmail(login.getEmail())
			.orElseThrow(InvalidSignInformation::new);

		PasswordEncoder encoder = new PasswordEncoder();


		boolean matches = encoder.matches(login.getPassword(), user.getPassword());
		if (!matches) {
			throw new InvalidSignInformation();
		}

		return user.getId();

	}

	public void signUp(Signup signup) {
		Optional<User> userOptional = userRepository.findByEmail(signup.getEmail());
		if (userOptional.isPresent()) {
			throw new AlreadyExistsEmailException();
		}

		PasswordEncoder encoder = new PasswordEncoder();

		String encryptedPassword = encoder.encode(signup.getPassword());

		User user = User.builder()
			.name(signup.getName())
			.password(encryptedPassword)
			.email(signup.getEmail())
			.build();

		userRepository.save(user);
	}
}
