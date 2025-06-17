package org.juniortown.backend.service;

import static org.mockito.Mockito.*;

import org.assertj.core.api.Assertions;
import org.juniortown.backend.user.entity.User;
import org.juniortown.backend.user.exception.AlreadyExistsEmailException;
import org.juniortown.backend.user.repository.UserRepository;
import org.juniortown.backend.user.request.SignUpDTO;
import org.juniortown.backend.user.service.AuthService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class AuthServiceTest {
	@Mock
	private UserRepository userRepository;
	@Mock
	private BCryptPasswordEncoder encoder;
	@InjectMocks
	private AuthService authService;
	private SignUpDTO dto;

	@BeforeEach
	void setUp() {
		dto = SignUpDTO.builder()
			.username("testUser")
			.email("test@eaxample.com")
			.password("password123")
			.build();
	}

	@AfterEach
	void clear() {
		userRepository.deleteAll();
	}

	@Test
	@DisplayName("회원가입 성공")
	void signup_success_whenEmailNotExists() {
		// given
		when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
		when(encoder.encode(dto.getPassword())).thenReturn("encodedPwd123");

		// when
		authService.signUp(dto);

		// then
		ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
		verify(userRepository).save(captor.capture());
		User saved = captor.getValue();
		Assertions.assertThat(saved.getName()).isEqualTo(dto.getUsername());
		Assertions.assertThat(saved.getEmail()).isEqualTo(dto.getEmail());
		Assertions.assertThat(saved.getPassword()).isEqualTo("encodedPwd123");

	}

	@Test
	@DisplayName("이미 존재하는 이메일의 경우 에외 발생")
	void signup_fail_whenEmailExists() {
		// given
		when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);

		// when, then
		Assertions.assertThatThrownBy(() -> authService.signUp(dto))
			.isInstanceOf(AlreadyExistsEmailException.class)
			.hasMessage("이미 가입된 이메일입니다.");

		verify(userRepository, never()).save(any());

	}
}