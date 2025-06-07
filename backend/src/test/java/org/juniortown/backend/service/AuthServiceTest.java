package org.juniortown.backend.service;

import static org.junit.jupiter.api.Assertions.*;

import org.juniortown.backend.crypto.PasswordEncoder;
import org.juniortown.backend.domain.User;
import org.juniortown.backend.exception.AlreadyExistsEmailException;
import org.juniortown.backend.exception.InvalidSignInformation;
import org.juniortown.backend.repository.UserRepository;
import org.juniortown.backend.request.Login;
import org.juniortown.backend.request.Signup;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

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
	@Test
	@DisplayName("로그인 성공")
	void test3() {
		// given
		PasswordEncoder encoder = new PasswordEncoder();
		String encryptedPassword = encoder.encode("3333");
		User user = User.builder()
			.email("test@gmail.com")
			.password(encryptedPassword)
			.name("짱똘맨")
			.build();
		userRepository.save(user);

		Login login = Login.builder()
			.email("test@gmail.com")
			.password("3333")
			.build();

		// when
		Long userId = authService.signIn(login);

		// then
		assertNotNull(userId);


	}

	@Test
	@DisplayName("로그인 시 비밀번호 틀림")
	void test4() {
		// given
		Signup signup = Signup.builder()
			.email("test@gmail.com")
			.password("3333")
			.name("curry")
			.build();
		authService.signUp(signup);

		Login login = Login.builder()
			.email("test@gmail.com")
			.password("1234")
			.build();

		// Expected
		assertThrows(InvalidSignInformation.class, () -> authService.signIn(login));





	}
}