package org.juniortown.backend.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Clock;
import java.util.UUID;

import org.juniortown.backend.post.entity.Post;
import org.juniortown.backend.post.repository.PostRepository;
import org.juniortown.backend.post.service.PostService;
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

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // 클래스 단위로 테스트 인스턴스를 생성한다.
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
public class PostControllerPagingTest {
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private PostRepository postRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private AuthService authService;
	@Autowired
	private JWTUtil jwtUtil;
	private static String jwt;
	private User testUser;
	@Autowired
	private Clock clock;

	@BeforeEach
	void clean_and_init() {
		postRepository.deleteAll();
		// 게시글 더미 데이터 생성
		for (int i = 0; i < 53; i++) {
			Post post = Post.builder()
				.user(testUser)
				.title("테스트 글 " + (i + 1))
				.content("테스트 내용 " + (i + 1))
				.build();
			postRepository.save(post);
		}
	}
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
	@DisplayName("글 목록 조회 - 첫 페이지 조회 성공")
	void get_posts_first_page_success() throws Exception {
		// given
		int page = 0; // 첫 페이지

		// expected
		mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/{page}", page)
				.header("Authorization", jwt)
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content", hasSize(10))) // 첫 페이지는 10개
			.andExpect(jsonPath("$.totalElements").value(53)) // 전체 게시글 수
			.andExpect(jsonPath("$.totalPages").value(6)) // 총 페이지 수
			.andExpect(jsonPath("$.hasPrevious").value(false))
			.andExpect(jsonPath("$.hasNext").value(true)) // 다음 페이지 있음
			.andExpect(jsonPath("$.page").value(0)) // 현재 페이지
			.andDo(print());
	}

	@Test
	@DisplayName("글 목록 조회(중간 페이지) - 두 번째 페이지 조회 성공")
	void get_posts_second_page_success() throws Exception {
		// given
		int page = 1; // 두 번째 페이지

		// expected
		mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/{page}", page)
				.header("Authorization", jwt)
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content", hasSize(10))) // 두 번째 페이지는 10개
			.andExpect(jsonPath("$.totalElements").value(53)) // 전체 게시글 수
			.andExpect(jsonPath("$.totalPages").value(6)) // 총 페이지 수
			.andExpect(jsonPath("$.hasPrevious").value(true)) // 이전 페이지 있음
			.andExpect(jsonPath("$.hasNext").value(true)) // 다음 페이지 있음
			.andExpect(jsonPath("$.page").value(1)) // 현재 페이지
			.andDo(print());
	}

	@Test
	@DisplayName("글 목록 조회 - 마지막 페이지 조회 성공")
	void get_posts_last_page_success() throws Exception {
		// given
		int page = 5; // 마지막 페이지 (53개 게시글, 페이지당 10개 -> 마지막 페이지는 6번째)

		// expected
		mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/{page}", page)
				.header("Authorization", jwt)
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content", hasSize(3))) // 마지막 페이지는 3개
			.andExpect(jsonPath("$.totalElements").value(53)) // 전체 게시글 수
			.andExpect(jsonPath("$.totalPages").value(6)) // 총 페이지 수
			.andExpect(jsonPath("$.hasPrevious").value(true)) // 이전 페이지 있음
			.andExpect(jsonPath("$.hasNext").value(false)) // 다음 페이지 없음
			.andExpect(jsonPath("$.page").value(5)) // 현재 페이지
			.andExpect(jsonPath("$.content.size()").value(3)) // 페이지당 게시글 수
			.andDo(print());
	}


	@Test
	@DisplayName("글 목록 조회 - 삭제된 게시글은 조회하지 않는다.")
	void get_posts_page_deleted_posts_not_included() throws Exception {
		// given
		int page = 5; // 첫 페이지

		Long postId = postRepository.findAll().get(0).getId();

		// 게시글 중 하나를 삭제 처리
		postRepository.findById(postId)
			.ifPresent(post -> {
				post.softDelete(clock); // soft delete 처리
				postRepository.save(post);
			});


		// expected
		mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/{page}", page)
				.header("Authorization", jwt)
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.totalElements").value(52)) // 전체 게시글 수에서 삭제된 게시글 제외
			.andDo(print());
	}


}
