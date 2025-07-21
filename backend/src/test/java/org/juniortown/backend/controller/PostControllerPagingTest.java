package org.juniortown.backend.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

import org.juniortown.backend.config.RedisTestConfig;
import org.juniortown.backend.like.entity.Like;
import org.juniortown.backend.like.repository.LikeRepository;
import org.juniortown.backend.like.service.LikeService;
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
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
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
@Import(RedisTestConfig.class)
@Transactional
public class PostControllerPagingTest {
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
	@Autowired
	private LikeService likeService;
	@Autowired
	private JWTUtil jwtUtil;
	private static String jwt;
	private User testUser1;
	private User testUser2;
	@Autowired
	private Clock clock;
	private static final int POST_COUNT = 53;

	@BeforeEach
	void clean_and_init() {
		// 게시글 더미 데이터 생성
		for (int i = 0; i < POST_COUNT; i++) {
			Post post = Post.builder()
				.user(testUser1)
				.title("테스트 글 " + (i + 1))
				.content("테스트 내용 " + (i + 1))
				.build();
			postRepository.save(post);
		}
	}
	@BeforeAll
	public void init() throws Exception {
		UUID uuid1 = UUID.randomUUID();
		UUID uuid2 = UUID.randomUUID();
		String email1 = uuid1 + "@naver.com";
		String email2 = uuid2 + "@naver.com";
		SignUpDTO signUpDTO1 = SignUpDTO.builder()
			.email(email1)
			.password("1234")
			.username("테스터1")
			.build();

		SignUpDTO signUpDTO2 = SignUpDTO.builder()
			.email(email2)
			.password("123456")
			.username("테스터2")
			.build();

		LoginDTO loginDTO = LoginDTO.builder()
			.email(signUpDTO1.getEmail())
			.password(signUpDTO1.getPassword())
			.build();
		authService.signUp(signUpDTO1);
		authService.signUp(signUpDTO2);
		testUser1 = userRepository.findByEmail(email1).get();
		testUser2 = userRepository.findByEmail(email2).get();



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
			.andExpect(jsonPath("$.totalElements").value(POST_COUNT)) // 전체 게시글 수
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
			.andExpect(jsonPath("$.totalElements").value(POST_COUNT)) // 전체 게시글 수
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
			.andExpect(jsonPath("$.totalElements").value(POST_COUNT)) // 전체 게시글 수
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

	@Test
	@DisplayName("좋아요 한 게시글 목록 조회 - 첫 페이지 조회")
	void like_post_and_get_posts_first_page_success() throws Exception {
		// given
		int page = 0; // 첫 페이지
		List<Post> posts = postRepository.findAll(Sort.by("createdAt").descending());
		Post testPost1 = posts.get(0);
		Post testPost2 = posts.get(1);

		Like like1 = Like.builder()
			.user(testUser1)
			.post(testPost1)
			.build();
		Like like2 = Like.builder()
			.user(testUser2)
			.post(testPost1)
			.build();
		Like like3 = Like.builder()
			.user(testUser1)
			.post(testPost2)
			.build();
		// 좋아요 생성
		likeRepository.saveAll(List.of(like1, like2, like3));

		// expected
		mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/{page}", page)
				.header("Authorization", jwt)
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content", hasSize(10))) // 첫 페이지는 10개
			.andExpect(jsonPath("$.content[0].id").value(testPost1.getId())) // 첫 번째 게시글 ID
			.andExpect(jsonPath("$.content[0].likeCount").value(2)) // 첫 번째 게시글 좋아요 수
			.andExpect(jsonPath("$.content[1].id").value(testPost2.getId())) // 두 번째 게시글 ID
			.andExpect(jsonPath("$.content[1].likeCount").value(1)) // 두 번째 게시글 좋아요 수
			.andExpect(jsonPath("$.totalElements").value(POST_COUNT)) // 전체 게시글 수
			.andExpect(jsonPath("$.totalPages").value(6)) // 총 페이지 수
			.andExpect(jsonPath("$.hasPrevious").value(false))
			.andExpect(jsonPath("$.hasNext").value(true)) // 다음 페이지 있음
			.andExpect(jsonPath("$.page").value(0)) // 현재 페이지
			.andDo(print());
	}

	@Test
	@DisplayName("좋아요 취소한 게시글 목록 조회 - 첫 페이지 조회")
	void unlike_post_and_get_posts_first_page_success() throws Exception {
		// given
		int page = 0; // 첫 페이지
		List<Post> posts = postRepository.findAll(Sort.by("createdAt").descending());
		Post testPost1 = posts.get(0);
		Post testPost2 = posts.get(1);

		Like like1 = Like.builder()
			.user(testUser1)
			.post(testPost1)
			.build();
		Like like2 = Like.builder()
			.user(testUser2)
			.post(testPost1)
			.build();
		Like like3 = Like.builder()
			.user(testUser1)
			.post(testPost2)
			.build();
		// 좋아요 생성
		likeRepository.saveAll(List.of(like1, like2, like3));

		// 좋아요 취소
		likeService.likePost(testUser1.getId(), testPost1.getId());
		likeService.likePost(testUser1.getId(), testPost2.getId());

		// expected
		mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/{page}", page)
				.header("Authorization", jwt)
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content", hasSize(10))) // 첫 페이지는 10개
			.andExpect(jsonPath("$.content[0].id").value(testPost1.getId())) // 첫 번째 게시글 ID
			.andExpect(jsonPath("$.content[0].likeCount").value(1)) // 첫 번째 게시글 좋아요 수
			.andExpect(jsonPath("$.content[1].id").value(testPost2.getId())) // 두 번째 게시글 ID
			.andExpect(jsonPath("$.content[1].likeCount").value(0)) // 두 번째 게시글 좋아요 수
			.andExpect(jsonPath("$.totalElements").value(POST_COUNT)) // 전체 게시글 수
			.andExpect(jsonPath("$.totalPages").value(6)) // 총 페이지 수
			.andExpect(jsonPath("$.hasPrevious").value(false))
			.andExpect(jsonPath("$.hasNext").value(true)) // 다음 페이지 있음
			.andExpect(jsonPath("$.page").value(0)) // 현재 페이지
			.andDo(print());
	}
}
