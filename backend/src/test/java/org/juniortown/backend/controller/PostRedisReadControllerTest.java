package org.juniortown.backend.controller;

import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;

import org.juniortown.backend.post.repository.PostRepository;
import org.juniortown.backend.user.dto.LoginDTO;
import org.juniortown.backend.user.entity.User;
import org.juniortown.backend.user.jwt.JWTUtil;
import org.juniortown.backend.user.repository.UserRepository;
import org.juniortown.backend.user.request.SignUpDTO;
import org.juniortown.backend.user.service.AuthService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // 클래스 단위로 테스트 인스턴스를 생성한다.
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
@Transactional
public class PostRedisReadController {
	@Autowired
	private MockMvc mockMvc;
	private PostRepository postRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private AuthService authService;
	@Autowired
	private JWTUtil jwtUtil;

	@BeforeEach
	void clean() {
		postRepository.deleteAll();
	}

	private static String jwt;
	private User testUser;

	@BeforeAll
	public void init() throws Exception {
		UUID uuid = UUID.randomUUID();
		String email = uuid + "@naver.com";
		SignUpDTO signUpDTO = SignUpDTO.builder()
			.email(email)
			.password("1234")
			.username("테스터")
			.build();

		LoginDTO loginDTO = LoginDTO.builder()
			.email(signUpDTO.getEmail())
			.password(signUpDTO.getPassword())
			.build();
		authService.signUp(signUpDTO);
		testUser = userRepository.findByEmail(email).get();

		// 로그인 후 JWT 토큰을 발급받는다.
		mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(loginDTO))
			)
			.andExpect(status().isOk())
			.andDo(result -> {
				jwt = result.getResponse().getHeader("Authorization");
			});
	}

	@Test
	@DisplayName("게시글 조회수 증가 성공 - 중복키 존재 x")
	void test1(){
		// 1.게시글 상세 조회할 때 조회수도 이제 반환해줘야 함
		// 2.조회수는 DB에 있는 값 + Redis에 있는 값이다.
		// 3.상세 조회 로직을 통해 redis에 있는 증분값이 증가한다.
		// 3-1. redis의 증분값이 여러 테스트에서 독립적으로 유지될지 모르겠네
		// 3-2?. testContainer를 쓰면 테스트마다 다른 레디스 컨테이너를 사용하나?
		// 4.먼저 redis 증분값을 증가시키고 조회수 값을 가져와야 한다.
	}

}
