package org.juniortown.backend.service;

import org.juniortown.backend.domain.Session;
import org.juniortown.backend.domain.User;
import org.juniortown.backend.exception.InvalidSignInformation;
import org.juniortown.backend.repository.UserRepository;
import org.juniortown.backend.request.Login;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
	private final UserRepository userRepository;
	@Transactional
	public String signIn(Login login) {
		User user = userRepository.findByEmailAndPassword(login.getEmail(),
				login.getPassword())
			.orElseThrow(InvalidSignInformation::new);

		Session session = user.addSession();
		return session.getAccessToken();

	}
}
