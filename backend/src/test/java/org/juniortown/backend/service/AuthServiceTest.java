package org.juniortown.backend.service;

import static org.junit.jupiter.api.Assertions.*;

import org.juniortown.backend.crypto.PasswordEncoder;
import org.juniortown.backend.domain.User;
import org.juniortown.backend.exception.AlreadyExistsEmailException;
import org.juniortown.backend.exception.InvalidSignInformation;
import org.juniortown.backend.repository.UserRepository;
import org.juniortown.backend.request.Signup;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
class AuthServiceTest {
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private AuthService authService;
	@AfterEach
	void clear() {
		userRepository.deleteAll();
	}

	@Test
	@DisplayName("회원가입 성공")
	@Transactional
	void test1() {
		// given
		Signup signup = Signup.builder()
			.email("test@gmail.com")
			.password("3333")
			.name("curry")
			.build();

		// when
		authService.signUp(signup);

		// then
		assertEquals(1, userRepository.count());

		User user = userRepository.findAll().iterator().next();
		assertEquals("test@gmail.com", user.getEmail());
		assertNotNull(user.getPassword());
		assertEquals("curry", user.getName());
	}

	@Test
	@DisplayName("회원가입 시 중복된 이메일")
	void test2() {
		// given
		User dupUser = User.builder()
			.email("test@gmail.com")
			.password("1234")
			.name("짱똘맨")
			.build();
		userRepository.save(dupUser);

		Signup signup = Signup.builder()
			.email("test@gmail.com")
			.password("3333")
			.name("curry")
			.build();

		// expect
		assertThrows(AlreadyExistsEmailException.class,
			() -> authService.signUp(signup));
	}
}