package org.juniortown.backend.like.controller;

import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;

import org.juniortown.backend.config.RedisTestConfig;
import org.juniortown.backend.config.SyncConfig;
import org.juniortown.backend.config.TestClockConfig;
import org.juniortown.backend.like.entity.Like;
import org.juniortown.backend.like.exception.LikeFailureException;
import org.juniortown.backend.like.repository.LikeRepository;
import org.juniortown.backend.post.entity.Post;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({RedisTestConfig.class, SyncConfig.class, TestClockConfig.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // 클래스 단위로 테스트 인스턴스를 생성한다.
@Transactional
class LikeControllerTest {
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private PostRepository postRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private LikeRepository likeRepository;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private AuthService authService;
	private static String jwt;
	private User testUser;
	private Post testPost;

	@BeforeEach
	void clean() {
		likeRepository.deleteAll();
		testPost = postRepository.save(Post.builder()
			.title("테스트 게시글")
			.content("테스트 내용입니다.")
			.user(testUser)
			.build());
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
	@DisplayName("좋아요 성공 테스트 - 성공")
	void likePost_success() throws Exception{
		// given
		Long postId = testPost.getId();

		// expected
		mockMvc.perform(MockMvcRequestBuilders.post("/api/posts/likes/" + postId)
				.header("Authorization", jwt)
			)
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.userId").value(testUser.getId()))
			.andExpect(jsonPath("$.postId").value(postId))
			.andExpect(jsonPath("$.isLiked").value(true));
	}

	@Test
	@DisplayName("좋아요 취소 테스트 - 성공")
	void unlikePost_success() throws Exception {
		// given
		Long postId = testPost.getId();
		// 미리 좋아요 생성
		likeRepository.save(Like.builder()
			.user(testUser)
			.post(testPost)
			.build());

		// expected
		mockMvc.perform(MockMvcRequestBuilders.post("/api/posts/likes/" + postId)
				.header("Authorization", jwt)
			)
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.userId").value(testUser.getId()))
			.andExpect(jsonPath("$.postId").value(postId))
			.andExpect(jsonPath("$.isLiked").value(false));
	}

	@Test
	@DisplayName("좋아요 예외 테스트 - 게시글 없음 예외")
	void likePost_post_should_exist() throws Exception {
		// given
		Long postId = 999L; // 존재하지 않는 게시글 ID

		// expected
		mockMvc.perform(MockMvcRequestBuilders.post("/api/posts/likes/" + postId)
				.header("Authorization", jwt)
			)
			.andExpect(status().is5xxServerError())
			.andExpect(jsonPath("$.code").value("500"))
			.andExpect(jsonPath("$.message").value(LikeFailureException.MESSAGE));
	}


}