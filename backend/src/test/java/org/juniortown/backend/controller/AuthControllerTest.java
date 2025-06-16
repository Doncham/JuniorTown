package org.juniortown.backend.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.juniortown.backend.user.repository.UserRepository;
import org.juniortown.backend.user.request.SignUpDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ObjectMapper objectMapper;

	@AfterEach
	void clean() {
		userRepository.deleteAll();
	}

	@Test
	@DisplayName("회원가입")
	void signupTest() throws Exception {
		// given
		SignUpDTO signUpDTO = SignUpDTO.builder()
			.email("test@gmail.com")
			.password("3333")
			.name("curry")
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
	void emailValidationTest() throws Exception {
		// given
		SignUpDTO signUpDTO = SignUpDTO.builder()
			.email("testgmail.com") // 이메일 형식이 잘못됨
			.password("3333")
			.name("curry")
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
	void passwordNotEmptyValidationTest() throws Exception {
		// given
		SignUpDTO signUpDTO = SignUpDTO.builder()
			.email("test@gmail.com")
			.password("")
			.name("curry")
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
	void nameNotEmptyValidationTest() throws Exception {
		// given
		SignUpDTO signUpDTO = SignUpDTO.builder()
			.email("test@gmail.com")
			.password("password")
			.name("")
			.build();

		// expected
		mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(signUpDTO)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.validation.name", hasSize(2)))
			.andExpect(jsonPath("$.validation.name", hasItems(
				"이름을 입력해주세요.",
				"이름은 2자 이상 20자 이하로 입력해주세요."
			)))
			.andDo(print());
	}

	@Test
	@DisplayName("이름은 2자 이상 20자 이하로 입력해야 함")
	void nameSizeBiggerThan2() throws Exception {
		// given
		SignUpDTO signUpDTO = SignUpDTO.builder()
			.email("test@gmail.com")
			.password("password")
			.name("a") // 1자
			.build();

		// expected
		mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(signUpDTO)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.validation.name").value("이름은 2자 이상 20자 이하로 입력해주세요."))
			.andDo(print());
	}

	@Test
	@DisplayName("이름은 2자 이상 20자 이하로 입력해야 함")
	void nameSizeSmallerThan20() throws Exception {
		// given
		SignUpDTO signUpDTO = SignUpDTO.builder()
			.email("test@gmail.com")
			.password("password")
			.name("a".repeat(21))
			.build();

		// expected
		mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(signUpDTO)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.validation.name").value("이름은 2자 이상 20자 이하로 입력해주세요."))
			.andDo(print());
	}
}