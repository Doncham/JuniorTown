package org.juniortown.backend.post.controller;

import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.juniortown.backend.comment.entity.Comment;
import org.juniortown.backend.comment.repository.CommentRepository;
import org.juniortown.backend.config.RedisTestConfig;
import org.juniortown.backend.config.SyncConfig;
import org.juniortown.backend.config.TestClockConfig;
import org.juniortown.backend.like.entity.Like;
import org.juniortown.backend.like.repository.LikeRepository;
import org.juniortown.backend.post.entity.Category;
import org.juniortown.backend.post.entity.Post;
import org.juniortown.backend.post.repository.PostRepository;
import org.juniortown.backend.post.service.ReadCountRedisService;
import org.juniortown.backend.user.entity.User;
import org.juniortown.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
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

import com.redis.testcontainers.RedisContainer;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({RedisTestConfig.class, TestClockConfig.class})
@Testcontainers
@Transactional
public class PostMainPageControllerTest {
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private PostRepository postRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	CommentRepository commentRepository;
	@Autowired
	LikeRepository likeRepository;
	@Autowired
	ReadCountRedisService readCountRedisService;

	private User u1, u2, u3, u4;
	private Post q1, q2, s1, s2;

	@Container
	static GenericContainer<?> redis = new RedisContainer(DockerImageName.parse("redis:8.0"))
		.withCommand("redis-server --port 6382")
		.withExposedPorts(6382);


	@DynamicPropertySource
	static void overrideProps(DynamicPropertyRegistry registry) {
		registry.add("spring.data.redis.host", redis::getHost);
		registry.add("spring.data.redis.port", () -> redis.getFirstMappedPort());
	}

	@BeforeEach
	void setUp() {
		u1 = userRepository.save(User.builder().name("u1").email("u1@test.com").password("p").build());
		u2 = userRepository.save(User.builder().name("u2").email("u2@test.com").password("p").build());
		u3 = userRepository.save(User.builder().name("u3").email("u3@test.com").password("p").build());
		u4 = userRepository.save(User.builder().name("u4").email("u4@test.com").password("p").build());

		// QUESTION 카테고리 2개
		q1 = postRepository.save(Post.builder().user(u1).title("Q1").content("Q1C").category(Category.QUESTION).build());
		q2 = postRepository.save(Post.builder().user(u2).title("Q2").content("Q2C").category(Category.QUESTION).build());

		// STUDY 카테고리 2개
		s1 = postRepository.save(Post.builder().user(u3).title("S1").content("S1C").category(Category.STUDY).build());
		s2 = postRepository.save(Post.builder().user(u4).title("S2").content("S2C").category(Category.STUDY).build());

		// Q1: like=2, comment=1
		likeRepository.saveAll(List.of(
			Like.builder().post(q1).user(u1).build(),
			Like.builder().post(q1).user(u2).build()
		));
		commentRepository.save(Comment.builder().post(q1).user(u3).username(u3.getName()).content("c").build());

		// Q2: like=1, comment=0
		likeRepository.save(Like.builder().post(q2).user(u3).build());

		// S1: like=0, comment=2
		commentRepository.saveAll(List.of(
			Comment.builder().post(s1).user(u1).username(u1.getName()).content("c1").build(),
			Comment.builder().post(s1).user(u2).username(u2.getName()).content("c2").build()
		));

		// S2: like=1, comment=0
		likeRepository.save(Like.builder().post(s2).user(u1).build());
	}

	@Test
	@DisplayName("홈 섹션 조회 성공: 카테고리별 Top-N과 좋아요/댓글 집계가 응답에 반영된다")
	void homepage_success() throws Exception {
		// when & then
		mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/home")
				.param("perCategory", "2")
				.accept(APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.sections").isArray())
			// 섹션 개수 = Category.values().length
			.andExpect(jsonPath("$.sections.length()").value(Category.values().length))
			// QUESTION 섹션이 존재하고 아이템이 최대 2개
			// 최신순 정렬(createdAt DESC, id DESC)에 따라 Q2가 첫 번째로 나와야 함
			.andExpect(jsonPath("$.sections[?(@.category=='question')]").isArray())
			.andExpect(jsonPath("$.sections[?(@.category=='question')].items.length()").value(2))
			// STUDY 섹션이 존재하고 아이템이 최대 2개
			.andExpect(jsonPath("$.sections[?(@.category=='study')]").isArray())
			.andExpect(jsonPath("$.sections[?(@.category=='study')].items.length()").value(2))
			// 특정 포스트의 집계 값 예시 검증 (Q1: like=2, comment=1)
			.andExpect(jsonPath("$.sections[?(@.category=='question')].items[0].title").value("Q2"))
			// STUDY 섹션 내 임의 하나의 아이템 필드 확인
			.andExpect(jsonPath("$.sections[?(@.category=='study')].items[0].postId").value(s2.getId().intValue()))
			.andDo(print());
	}

	@Test
	@DisplayName("perCategory 제한: 1로 요청하면 섹션별 최대 1건만 노출")
	void perCategory_limit_works() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/home")
				.param("perCategory", "1")
				.accept(APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.sections").isArray())
			// question 섹션 아이템 길이 <= 1
			.andExpect(jsonPath("$.sections[?(@.category=='question')].items.length()").value(1))
			// study 섹션 아이템 길이 <= 1
			.andExpect(jsonPath("$.sections[?(@.category=='study')].items.length()").value(1))
			.andDo(print());
	}

	@Test
	@DisplayName("모든 카테고리가 비어있으면 sections는 빈 배열로 응답한다")
	void homepage_all_empty() throws Exception {
		// given: 모두 삭제 (트랜잭션 내에서 안전)
		likeRepository.deleteAll();
		commentRepository.deleteAll();
		postRepository.deleteAll();

		mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/home")
				.param("perCategory", "5")
				.accept(APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.sections").isArray())
			.andExpect(jsonPath("$.sections.length()").value(0))
			.andDo(print());
	}

}
