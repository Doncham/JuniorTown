package org.juniortown.backend.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.hamcrest.Matchers;
import org.juniortown.backend.domain.Session;
import org.juniortown.backend.domain.User;
import org.juniortown.backend.repository.SessionRepository;
import org.juniortown.backend.repository.UserRepository;
import org.juniortown.backend.request.Login;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
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
	@Autowired
	private SessionRepository sessionRepository;

	@BeforeEach
	void clean() {
		userRepository.deleteAll();
	}

	@Test
	@DisplayName("로그인 성공")
	void test1() throws Exception {
		// given
		userRepository.save(User.builder()
			.name("테스트맨")
			.email("sangwon@gmali.com")
			.password("1234")
			.build());

		Login login = Login.builder()
			.email("sangwon@gmali.com")
			.password("1234")
			.build();

		String json = objectMapper.writeValueAsString(login);

		// expected
		mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isOk())
			.andDo(print());

	}
	@Test
	@Transactional
	@DisplayName("로그인 성공 후 세션 1개 생성")
	void test2() throws Exception {
		// given
		User user = userRepository.save(User.builder()
			.name("테스트맨")
			.email("sangwon@gmali.com")
			.password("1234")
			.build());

		Login login = Login.builder()
			.email("sangwon@gmali.com")
			.password("1234")
			.build();

		String json = objectMapper.writeValueAsString(login);

		// expected
		mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isOk())
			.andDo(print());

		Assertions.assertEquals(1L, user.getSessions().stream().count());
	}

	@Test
	@Transactional
	@DisplayName("로그인 성공 후 세션 응답")
	void test3() throws Exception {
		// given
		User user = userRepository.save(User.builder()
			.name("테스트맨")
			.email("sangwon@gmali.com")
			.password("1234")
			.build());

		Login login = Login.builder()
			.email("sangwon@gmali.com")
			.password("1234")
			.build();

		String json = objectMapper.writeValueAsString(login);

		// expected
		mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.accessToken", Matchers.notNullValue()))
			.andDo(print());

	}

	@Test
	@DisplayName("로그인 후 권한이 필요한 페이지 접속 /foo ")
	void test4() throws Exception {
		User user = User.builder()
			.name("테스트맨")
			.email("sangwon@gmali.com")
			.password("1234")
			.build();
		Session session = user.addSession();
		userRepository.save(user);

		// expected
		mockMvc.perform(MockMvcRequestBuilders.get("/foo")
				.header("Authorization", session.getAccessToken())
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(print());
	}

	@Test
	@DisplayName("로그인 후 검증되지 않은 세션값으로 권한이 필요한 페이지 접속할 수 없다.")
	void test5() throws Exception {
		User user = User.builder()
			.name("테스트맨")
			.email("sangwon@gmali.com")
			.password("1234")
			.build();
		Session session = user.addSession();
		userRepository.save(user);

		// expected
		mockMvc.perform(MockMvcRequestBuilders.get("/foo")
				.header("Authorization", session.getAccessToken() +"-o")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isUnauthorized())
			.andDo(print());
	}
}