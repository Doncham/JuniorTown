package org.juniortown.backend.controller;

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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
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
	@Transactional
	void test6() throws Exception {
		// given
		SignUpDTO signUpDTO = SignUpDTO.builder()
			.email("test@gmail.com")
			.password("3333")
			.name("curry")
			.build();

		// expected
		mockMvc.perform(MockMvcRequestBuilders.post("/auth/signup")
				.content(objectMapper.writeValueAsString(signUpDTO))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(print());
	}
}