package org.juniortown.backend.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.juniortown.backend.comment.entity.Comment;
import org.juniortown.backend.comment.repository.CommentRepository;
import org.juniortown.backend.config.RedisTestConfig;
import org.juniortown.backend.config.TestClockConfig;
import org.juniortown.backend.post.entity.Post;
import org.juniortown.backend.post.repository.PostRepository;
import org.juniortown.backend.post.service.ViewCountSyncService;
import org.juniortown.backend.user.dto.LoginDTO;
import org.juniortown.backend.user.entity.User;
import org.juniortown.backend.user.repository.UserRepository;
import org.juniortown.backend.user.request.SignUpDTO;
import org.juniortown.backend.user.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redis.testcontainers.RedisContainer;

import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
//@TestInstance(TestInstance.Lifecycle.PER_CLASS) // 클래스 단위로 테스트 인스턴스를 생성한다.
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
@Transactional
@Testcontainers
@Import({RedisTestConfig.class, TestClockConfig.class})
public class PostRedisReadControllerTest {
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private PostRepository postRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private CommentRepository commentRepository;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private AuthService authService;
	@Autowired
	private ViewCountSyncService viewCountSyncService;
	@Autowired
	@Qualifier("readCountRedisTemplate")
	private RedisTemplate<String, Long> readCountRedisTemplate;
	@Autowired
	private RedissonClient redissonClient;
	@Autowired
	private Clock clock;
	Post testPost;

	@Container
	static GenericContainer<?> redis = new RedisContainer(DockerImageName.parse("redis:8.0"))
		.withCommand("redis-server --port 6380")
		.withExposedPorts(6380);


	@DynamicPropertySource
	static void overrideProps(DynamicPropertyRegistry registry) {
		registry.add("spring.data.redis.host", redis::getHost);
		registry.add("spring.data.redis.port", () -> redis.getFirstMappedPort());
	}

	private static String jwt;
	private User testUser;

	@BeforeEach
	public void init() throws Exception {
		// System.out.println("Redis Host: " + redisHost);
		// System.out.println("Redis Port: " + redisPort);
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

		Post post = Post.builder()
			.user(testUser)
			.title("테스트 글")
			.content("테스트 내용")
			.build();

		testPost = postRepository.save(post);

		// 로그인 후 JWT 토큰을 발급받는다.
		mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(loginDTO))
			)
			.andExpect(status().isOk())
			.andDo(result -> {
				jwt = result.getResponse().getHeader("Authorization");
			});

		// 댓글 생성
		Comment parentComment1 = Comment.builder()
			.post(testPost)
			.user(testUser)
			.username(testUser.getName())
			.content("I'm Parent Comment1")
			.build();
		Comment parentComment2 = Comment.builder()
			.post(testPost)
			.user(testUser)
			.username(testUser.getName())
			.content("I'm Parent Comment2")
			.build();
		Comment childComment1 = Comment.builder()
			.post(testPost)
			.user(testUser)
			.username(testUser.getName())
			.content("I'm Child Comment1")
			.parent(parentComment1)
			.build();
		Comment childComment2 = Comment.builder()
			.post(testPost)
			.user(testUser)
			.username(testUser.getName())
			.content("I'm Child Comment2")
			.parent(parentComment2)
			.build();
		commentRepository.saveAll(List.of(
			parentComment1,
			parentComment2,
			childComment1,
			childComment2
		));
	}

	@Test
	@DisplayName("게시글 조회수 증가 성공(회원) - 중복키 존재 x")
	// d이놈
	void read_count_increase_with_user() throws Exception {
		// 1.게시글 상세 조회할 때 조회수도 이제 반환해줘야 함
		// 2.조회수는 DB에 있는 값 + Redis에 있는 값이다.
		// 3.상세 조회 로직을 통해 redis에 있는 증분값이 증가한다.
		// 3-1. redis의 증분값이 여러 테스트에서 독립적으로 유지될지 모르겠네
		// 3-2?. testContainer를 쓰면 테스트마다 다른 레디스 컨테이너를 사용하나?
		// 4.먼저 redis 증분값을 증가시키고 조회수 값을 가져와야 한다.

		Long postId = testPost.getId();

		mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/details/{postId}", postId)
				.contentType(APPLICATION_JSON)
				.header("Authorization", jwt)
			)
			.andExpect(status().isOk())
			.andDo(print())
			.andExpect(jsonPath("$.content").value("테스트 내용"))
			.andExpect(jsonPath("$.readCount").value(1));
	}

	// 게시글 조회수 증가 테스트를 회원/비회원을 나눠서 테스트해야하나? ㅇㅇ 복붙하면 되지


	@Test
	@DisplayName("게시글 조회수 증가 성공(비회원) - 중복키 존재 x")
	void read_count_increase_with_non_user() throws Exception {
		Long postId = testPost.getId();

		mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/details/{postId}", postId)
				.contentType(APPLICATION_JSON)
				.cookie(new Cookie("guestId", "testUUId"))
			)
			.andExpect(status().isOk())
			.andDo(print())
			.andExpect(jsonPath("$.content").value("테스트 내용"))
			.andExpect(jsonPath("$.readCount").value(1));
	}

	@Test
	@DisplayName("게시글 조회 2번 -> 1번만 조회수 증가(회원) - 중복키 존재 o")
	void dup_key_prevent_read_count_increase() throws Exception {
		Long postId = testPost.getId();

		mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/details/{postId}", postId)
			.contentType(APPLICATION_JSON)
			.header("Authorization", jwt)
		).andReturn();

		mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/details/{postId}", postId)
				.contentType(APPLICATION_JSON)
				.header("Authorization", jwt)
			)
			.andExpect(status().isOk())
			.andDo(print())
			.andExpect(jsonPath("$.content").value("테스트 내용"))
			.andExpect(jsonPath("$.readCount").value(1))
			.andExpect(jsonPath("$.userId").value(testUser.getId()))
			.andExpect(jsonPath("$.userName").value(testUser.getName()))
			.andExpect(jsonPath("$.isLiked").value(false))
			.andExpect(jsonPath("$.likeCount").value(0))
		    .andExpect(jsonPath("$.createdAt").exists())
			.andExpect(jsonPath("$.updatedAt").exists())
			.andExpect(jsonPath("$.deletedAt").doesNotExist())
			.andExpect(jsonPath("$.comments").isArray())
			.andExpect(jsonPath("$.comments.length()").value(2))
			.andExpect(jsonPath("$.comments[0].content").value("I'm Parent Comment1"))
			.andExpect(jsonPath("$.comments[0].children[0].content").value("I'm Child Comment1"))
			.andExpect(jsonPath("$.comments[1].content").value("I'm Parent Comment2"))
			.andExpect(jsonPath("$.comments[1].children[0].content").value("I'm Child Comment2"));
	}

	@Test
	@DisplayName("게시글 조회 2번 -> 1번만 조회수 증가(비회원) - 중복키 존재 o")
	void dup_key_prevent_read_count_increase_with_non_user() throws Exception {
		Long postId = testPost.getId();

		mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/details/{postId}", postId)
			.contentType(APPLICATION_JSON)

		).andReturn();

		mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/details/{postId}", postId)
				.contentType(APPLICATION_JSON)
			)
			.andExpect(status().isOk())
			.andDo(print())
			.andExpect(jsonPath("$.content").value("테스트 내용"))
			.andExpect(jsonPath("$.readCount").value(1));
	}

	@Test
	@DisplayName("게시글 조회 2번(회원 + 비회원 조회) -> readCount:2")
	void two_user_read_api_make_get_two_read_count() throws Exception {
		Long postId = testPost.getId();

		// 비회원 조회
		mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/details/{postId}", postId)
			.contentType(APPLICATION_JSON)
			.cookie(new Cookie("guestId", "testUUId"))
		).andReturn();

		// 회원 조회
		mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/details/{postId}", postId)
				.contentType(APPLICATION_JSON)
				.header("Authorization", jwt)
			)
			.andExpect(status().isOk())
			.andDo(print())
			.andExpect(jsonPath("$.content").value("테스트 내용"))
			.andExpect(jsonPath("$.readCount").value(2));
	}
	@Test
	@DisplayName("글 상세 조회 성공")
	void getPostDetail_success() throws Exception {
		// given
		Long postId = testPost.getId();


		// expected
		mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/details/{postId}", postId)
				.contentType(APPLICATION_JSON)
				.header("Authorization", jwt)
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content").value("테스트 내용"))
			.andDo(print());
	}

	@Test
	@DisplayName("글 상세 조회 실패 - 존재하지 않는 게시글")
	void getPostDetail_nonExistPost_failure() throws Exception {
		// given
		Long postId = 0L;

		// expected
		mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/details/{postId}" ,postId)
				.contentType(APPLICATION_JSON)
				.header("Authorization", jwt)
			)
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("404"))
			.andExpect(jsonPath("$.message").value("해당 게시글을 찾을 수 없습니다."))
			.andDo(print());
	}

	@Test
	@DisplayName("조회수 집계 실행 @Scheduled")
	void view_count_sync_success() {
		// given
		Long postId = testPost.getId();

		String key = "post:viewCount:" + postId;
		readCountRedisTemplate.opsForValue().set(key, 10L);

		// when
		viewCountSyncService.syncViewCounts();
		Post result = postRepository.findById(postId).get();

		// then
		assertEquals(10L, result.getReadCount());
		assertFalse(readCountRedisTemplate.hasKey(key));
	}

	// @Test
	// @DisplayName("조회수 집계 실행 - 동기화 락 획득 실패")
	// void view_count_sync_lock_fail() throws InterruptedException {
	// 	// given
	// 	Long postId = testPost.getId();
	// 	String key = "post:viewCount:" + postId;
	// 	readCountRedisTemplate.opsForValue().set(key, 10L);
	//
	// 	// when
	// 	// 동기화 락을 획득하지 못하는 상황을 시뮬레이션하기 위해, 다른 인스턴스에서 락을 획득했다고 가정
	// 	redissonClient.getLock(ViewCountSyncService.SYNC_LOCK_KEY).tryLock(10, 300, TimeUnit.SECONDS);
	// 	new Thread(() -> {
	// 		try {
	// 			viewCountSyncService.syncViewCounts();
	// 		} catch (Exception e) {
	// 			// 예외가 발생하면 테스트가 실패하지 않도록 처리
	// 			e.printStackTrace();
	// 		}
	// 	}).start();
	// 	//viewCountSyncService.syncViewCounts();
	// 	Thread.sleep(10000);
	//
	// 	// then
	// 	//assertTrue(readCountRedisTemplate.hasKey(key));
	// 	assertEquals(10L, readCountRedisTemplate.opsForValue().get(key));
	// }

	@Test
	@DisplayName("게시글 조회 성공 With 댓글 트리 구조")
	void post_details_read_with_comments_success() throws Exception {
		Long postId = testPost.getId();
		mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/details/{postId}", postId)
				.contentType(APPLICATION_JSON)
				.header("Authorization", jwt)
			)

			.andExpect(status().isOk())
			.andDo(print())
			.andExpect(jsonPath("$.comments").isArray())
			.andExpect(jsonPath("$.comments.length()").value(2))
			.andExpect(jsonPath("$.comments[0].content").value("I'm Parent Comment1"))
			.andExpect(jsonPath("$.comments[0].username").value("테스터"))
			.andExpect(jsonPath("$.comments[0].deletedAt").isEmpty())
			.andExpect(jsonPath("$.comments[0].createdAt").value(LocalDateTime.now(clock).toString()))
			.andExpect(jsonPath("$.comments[0].updatedAt").value(LocalDateTime.now(clock).toString()))

			.andExpect(jsonPath("$.comments[0].children[0].content").value("I'm Child Comment1"))
			.andExpect(jsonPath("$.comments[0].children[0].username").value("테스터"))
			.andExpect(jsonPath("$.comments[0].children[0].deletedAt").isEmpty())
			.andExpect(jsonPath("$.comments[0].children[0].createdAt").value(LocalDateTime.now(clock).toString()))
			.andExpect(jsonPath("$.comments[0].children[0].updatedAt").value(LocalDateTime.now(clock).toString()))

			.andExpect(jsonPath("$.comments[1].content").value("I'm Parent Comment2"))
			.andExpect(jsonPath("$.comments[1].username").value("테스터"))
			.andExpect(jsonPath("$.comments[1].deletedAt").isEmpty())
			.andExpect(jsonPath("$.comments[1].createdAt").value(LocalDateTime.now(clock).toString()))
			.andExpect(jsonPath("$.comments[1].updatedAt").value(LocalDateTime.now(clock).toString()))

			.andExpect(jsonPath("$.comments[1].children[0].content").value("I'm Child Comment2"))
			.andExpect(jsonPath("$.comments[1].children[0].username").value("테스터"))
			.andExpect(jsonPath("$.comments[1].children[0].deletedAt").isEmpty())
			.andExpect(jsonPath("$.comments[1].children[0].createdAt").value(LocalDateTime.now(clock).toString()))
			.andExpect(jsonPath("$.comments[1].children[0].updatedAt").value(LocalDateTime.now(clock).toString()));

	}

	@Test
	@DisplayName("게시글 조회 성공 With 댓글 트리 구조 - 댓글 1개 삭제")
	void post_details_read_with_one_comment_deleted_success() throws Exception {
		Long postId = testPost.getId();
		List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtAsc(
			postId);
		// 나중에 테스트 케이스 더 추가되면 무조건 깨지긴해
		Long childComment2 = comments.get(3).getId();

		mockMvc.perform(MockMvcRequestBuilders.delete("/api/comments/{commentId}", childComment2)
				.contentType(APPLICATION_JSON)
				.header("Authorization", jwt)
			)
			.andExpect(status().isNoContent());

		mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/details/{postId}", postId)
				.contentType(APPLICATION_JSON)
				.header("Authorization", jwt)
			)

			.andExpect(status().isOk())
			.andDo(print())
			.andExpect(jsonPath("$.comments").isArray())
			.andExpect(jsonPath("$.comments.length()").value(2))
			.andExpect(jsonPath("$.comments[0].content").value("I'm Parent Comment1"))
			.andExpect(jsonPath("$.comments[0].username").value("테스터"))
			.andExpect(jsonPath("$.comments[0].deletedAt").isEmpty())

			.andExpect(jsonPath("$.comments[0].children[0].content").value("I'm Child Comment1"))
			.andExpect(jsonPath("$.comments[0].children[0].username").value("테스터"))
			.andExpect(jsonPath("$.comments[0].children[0].deletedAt").isEmpty())

			.andExpect(jsonPath("$.comments[1].content").value("I'm Parent Comment2"))
			.andExpect(jsonPath("$.comments[1].username").value("테스터"))
			.andExpect(jsonPath("$.comments[1].deletedAt").isEmpty())

			.andExpect(jsonPath("$.comments[1].children[0].content").value("I'm Child Comment2"))
			.andExpect(jsonPath("$.comments[1].children[0].username").value("테스터"))
			.andExpect(jsonPath("$.comments[1].children[0].deletedAt").value(LocalDateTime.now(clock).toString()));

	}

}
