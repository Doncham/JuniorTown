package org.juniortown.backend.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.juniortown.backend.config.RedisTestConfig;
import org.juniortown.backend.config.SyncConfig;
import org.juniortown.backend.user.dto.LoginDTO;
import org.juniortown.backend.user.jwt.JWTUtil;
import org.juniortown.backend.user.repository.UserRepository;
import org.juniortown.backend.user.request.SignUpDTO;
import org.juniortown.backend.user.service.AuthService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({RedisTestConfig.class,SyncConfig.class})
class AuthControllerTest {
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private AuthService authService;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private JWTUtil jwtUtil;

	@AfterEach
	void clean() {
		userRepository.deleteAll();
	}

	@BeforeEach
	public void init() {
		SignUpDTO signUpDTO = SignUpDTO.builder()
			.email("init@gmail.com")
			.password("3333")
			.username("init")
			.build();
		authService.signUp(signUpDTO);
	}

	@Test
	@DisplayName("회원가입")
	void signup_test() throws Exception {
		// given
		SignUpDTO signUpDTO = SignUpDTO.builder()
			.email("test@gmail.com")
			.password("3333")
			.username("curry")
			.build();

		// expected
		mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(signUpDTO)))
			.andExpect(status().isCreated())
			.andDo(print());
	}

	@Test
	@DisplayName("이메일 형식이 아닌 경우 회원가입 실패")
	void email_validation_test() throws Exception {
		// given
		SignUpDTO signUpDTO = SignUpDTO.builder()
			.email("testgmail.com") // 이메일 형식이 잘못됨
			.password("3333")
			.username("curry")
			.build();

		// expected
		mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(signUpDTO)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.validation.email").value("유효한 이메일 형식이어야 합니다."))
			.andDo(print());
	}

	@Test
	@DisplayName("비밀번호는 빈칸이 아니어야 함")
	void password_not_empty_validation_test() throws Exception {
		// given
		SignUpDTO signUpDTO = SignUpDTO.builder()
			.email("test@gmail.com")
			.password("")
			.username("curry")
			.build();

		// expected
		mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(signUpDTO)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.validation.password").value("비밀번호를 입력해주세요."))
			.andDo(print());
	}

	@Test
	@DisplayName("이름은 빈칸이 아니어야 함")
	void name_not_empty_validation_test() throws Exception {
		// given
		SignUpDTO signUpDTO = SignUpDTO.builder()
			.email("test@gmail.com")
			.password("password")
			.username("")
			.build();

		// expected
		mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(signUpDTO)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.validation.username", hasSize(2)))
			.andExpect(jsonPath("$.validation.username", hasItems(
				"이름을 입력해주세요.",
				"이름은 2자 이상 20자 이하로 입력해주세요."
			)))
			.andDo(print());
	}

	@Test
	@DisplayName("이름은 2자 이상 20자 이하로 입력해야 함")
	void name_size_bigger_than() throws Exception {
		// given
		SignUpDTO signUpDTO = SignUpDTO.builder()
			.email("test@gmail.com")
			.password("password")
			.username("a") // 1자
			.build();

		// expected
		mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(signUpDTO)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.validation.username").value("이름은 2자 이상 20자 이하로 입력해주세요."))
			.andDo(print());
	}

	@Test
	@DisplayName("이름은 2자 이상 20자 이하로 입력해야 함")
	void name_size_smaller_than20() throws Exception {
		// given
		SignUpDTO signUpDTO = SignUpDTO.builder()
			.email("test@gmail.com")
			.password("password")
			.username("a".repeat(21))
			.build();

		// expected
		mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(signUpDTO)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.validation.username").value("이름은 2자 이상 20자 이하로 입력해주세요."))
			.andDo(print());
	}


	@Test
	@DisplayName("로그인 성공 시 Authorization 헤더에 JWT 토큰이 포함되어야 함")
	// 좀 분리해야 할거 같기도 함.
	void login_success_test() throws Exception {
		// given
		LoginDTO loginDTO = LoginDTO.builder()
			.email("init@gmail.com")
			.password("3333")
			.build();

		// expected
		MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(loginDTO)))
			.andExpect(status().isOk())
			.andExpect(header().exists("Authorization"))
			.andExpect(header().string("Authorization", startsWith("Bearer")))
			.andExpect(jsonPath("$.message").value("Login successful"))
			.andDo(print())
			.andReturn();

		// Authorization 헤더에서 JWT 토큰을 추출하고 검증
		String Token = mvcResult.getResponse().getHeader("Authorization");
		String extractedUsername = jwtUtil.getUsername(Token.split(" ")[1]);
		Assertions.assertEquals("init@gmail.com", extractedUsername);
		Assertions.assertFalse(jwtUtil.isExpired(Token.split(" ")[1]));

	}

	@Test
	@DisplayName("로그인 실패 - 잘못된 유저 정보를 통한 로그인의 경우")
	void login_failure_with_wrong_password() throws Exception {
		// given
		LoginDTO loginDTO = LoginDTO.builder()
			.email("init@gmail.com")
			.password("WrongPassword")
			.build();

		// expected
		mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(loginDTO)))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.message").value("Login failed"))
			.andDo(print());


	}
}