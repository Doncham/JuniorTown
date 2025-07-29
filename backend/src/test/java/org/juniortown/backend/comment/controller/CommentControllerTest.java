package org.juniortown.backend.comment.controller;

import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;

import org.juniortown.backend.comment.dto.request.CommentCreateRequest;
import org.juniortown.backend.comment.entity.Comment;
import org.juniortown.backend.comment.repository.CommentRepository;
import org.juniortown.backend.config.RedisTestConfig;
import org.juniortown.backend.config.SyncConfig;
import org.juniortown.backend.post.entity.Post;
import org.juniortown.backend.post.repository.PostRepository;
import org.juniortown.backend.user.dto.LoginDTO;
import org.juniortown.backend.user.entity.User;
import org.juniortown.backend.user.jwt.JWTUtil;
import org.juniortown.backend.user.repository.UserRepository;
import org.juniortown.backend.user.request.SignUpDTO;
import org.juniortown.backend.user.service.AuthService;
import org.junit.jupiter.api.BeforeAll;
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
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // 클래스 단위로 테스트 인스턴스를 생성한다.
@Import({RedisTestConfig.class, SyncConfig.class})
@Transactional
class CommentControllerTest {
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
	private JWTUtil jwtUtil;
	private static String jwt;
	private User testUser;
	private Post post;

	private final static Long ID_NOT_EXIST = 999999L;

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

		// 게시글을 하나 생성한다.
		post = postRepository.save(
			Post.builder()
				.title("테스트 게시글")
				.content("테스트 내용입니다.")
				.user(testUser)
				.build()
		);
		postRepository.save(post);
	}

	@Test
	@DisplayName("댓글 생성 성공")
	void create_comment_success() throws Exception {
		CommentCreateRequest commentRequest = CommentCreateRequest.builder()
			.content("테스트 댓글")
			.postId(post.getId())
			.parentId(null) // 최상위 댓글인 경우 null
			.build();

		mockMvc.perform(MockMvcRequestBuilders.post("/api/comments")
				.contentType(APPLICATION_JSON)
				.header("Authorization", jwt)
				.content(objectMapper.writeValueAsString(commentRequest))
			)
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.commentId").exists())
			.andExpect(jsonPath("$.content").value("테스트 댓글"))
			.andExpect(jsonPath("$.postId").value(post.getId()))
			.andExpect(jsonPath("$.parentId").doesNotExist())
			.andExpect(jsonPath("$.userId").value(testUser.getId()))
			.andExpect(jsonPath("$.username").value(testUser.getName()))
			.andExpect(jsonPath("$.createdAt").exists())
			.andDo(print());
	}

	@Test
	@DisplayName("대댓글 생성 성공")
	void create_child_comment_success() throws Exception {
		Comment parentComment = Comment.builder()
			.content("부모 댓글")
			.post(post)
			.user(testUser)
			.username(testUser.getName())
			.build();
		parentComment = commentRepository.save(parentComment);

		CommentCreateRequest commentRequest = CommentCreateRequest.builder()
			.content("테스트 댓글")
			.postId(post.getId())
			.parentId(parentComment.getId()) // 부모 댓글 ID
			.build();

		mockMvc.perform(MockMvcRequestBuilders.post("/api/comments")
				.contentType(APPLICATION_JSON)
				.header("Authorization", jwt)
				.content(objectMapper.writeValueAsString(commentRequest))
			)
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.commentId").exists())
			.andExpect(jsonPath("$.content").value("테스트 댓글"))
			.andExpect(jsonPath("$.postId").value(post.getId()))
			.andExpect(jsonPath("$.parentId").value(parentComment.getId()))
			.andExpect(jsonPath("$.userId").value(testUser.getId()))
			.andExpect(jsonPath("$.username").value(testUser.getName()))
			.andExpect(jsonPath("$.createdAt").exists())
			.andDo(print());
	}

	@Test
	@DisplayName("댓글 생성 실패 - 게시글이 존재하지 않음")
	void create_comment_fail_with_no_post() throws Exception {
		CommentCreateRequest commentRequest = CommentCreateRequest.builder()
			.content("테스트 댓글")
			.postId(ID_NOT_EXIST)
			.parentId(null) // 최상위 댓글인 경우 null
			.build();

		mockMvc.perform(MockMvcRequestBuilders.post("/api/comments")
				.contentType(APPLICATION_JSON)
				.header("Authorization", jwt)
				.content(objectMapper.writeValueAsString(commentRequest))
			)
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.message").value("해당 게시글을 찾을 수 없습니다."))
			.andDo(print());
	}

	@Test
	@DisplayName("댓글 생성 실패 - 유저가 존재하지 않음")
	void create_comment_fail_with_no_user() throws Exception {
		CommentCreateRequest commentRequest = CommentCreateRequest.builder()
			.content("테스트 댓글")
			.postId(post.getId())
			.parentId(null) // 최상위 댓글인 경우 null
			.build();
		userRepository.deleteById(testUser.getId()); // 유저 삭제

		mockMvc.perform(MockMvcRequestBuilders.post("/api/comments")
				.contentType(APPLICATION_JSON)
				.header("Authorization", jwt)
				.content(objectMapper.writeValueAsString(commentRequest))
			)
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.message").value("해당 사용자를 찾을 수 없습니다."))
			.andDo(print());
	}

	@Test
	@DisplayName("댓글 생성 실패 - 부모 댓글과 자식 댓글의 게시글이 다름")
	void create_comment_fail_with_not_same_post() throws Exception {
		Post dummyPost = Post.builder()
			.title("다른 게시글")
			.content("다른 게시글 내용입니다.")
			.user(testUser)
			.build();
		Post savedDummyPost = postRepository.save(dummyPost);

		Comment parentComment = Comment.builder()
			.content("부모 댓글")
			.post(savedDummyPost)
			.user(testUser)
			.username(testUser.getName())
			.build();
		Comment savedParentComment = commentRepository.save(parentComment);

		CommentCreateRequest commentRequest = CommentCreateRequest.builder()
			.content("테스트 댓글")
			.postId(post.getId())
			.parentId(savedParentComment.getId()) // 최상위 댓글인 경우 null
			.build();

		mockMvc.perform(MockMvcRequestBuilders.post("/api/comments")
				.contentType(APPLICATION_JSON)
				.header("Authorization", jwt)
				.content(objectMapper.writeValueAsString(commentRequest))
			)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("부모 댓글의 게시글과 대댓글이 속한 게시글이 일치하지 않습니다."))
			.andDo(print());
	}

	@Test
	@DisplayName("댓글 생성 실패 - 부모 댓글이 존재하지 않음.")
	void create_comment_fail_with_parent_comment_not_exist() throws Exception {
		Comment parentComment = Comment.builder()
			.content("부모 댓글")
			.post(post)
			.user(testUser)
			.username(testUser.getName())
			.build();
		Comment savedParentComment = commentRepository.save(parentComment);

		CommentCreateRequest commentRequest = CommentCreateRequest.builder()
			.content("테스트 댓글")
			.postId(post.getId())
			.parentId(ID_NOT_EXIST) // 최상위 댓글인 경우 null
			.build();

		mockMvc.perform(MockMvcRequestBuilders.post("/api/comments")
				.contentType(APPLICATION_JSON)
				.header("Authorization", jwt)
				.content(objectMapper.writeValueAsString(commentRequest))
			)
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.message").value("해당 댓글을 찾을 수 없습니다."))
			.andDo(print());
	}

	@Test
	@DisplayName("댓글 생성 실패 - content가 비어있음")
	void create_comment_fail_empty_content() throws Exception {
		CommentCreateRequest commentRequest = CommentCreateRequest.builder()
			.content("")
			.postId(post.getId())
			.parentId(null) // 최상위 댓글인 경우 null
			.build();

		mockMvc.perform(MockMvcRequestBuilders.post("/api/comments")
				.contentType(APPLICATION_JSON)
				.header("Authorization", jwt)
				.content(objectMapper.writeValueAsString(commentRequest))
			)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.validation.content").value("댓글 내용을 입력해주세요."))
			.andDo(print());
	}

	@Test
	@DisplayName("댓글 생성 실패 - postId가 비어있음")
	void create_comment_fail_empty_post_id() throws Exception {
		CommentCreateRequest commentRequest = CommentCreateRequest.builder()
			.content("테스트 댓글")
			.postId(null)
			.parentId(null) // 최상위 댓글인 경우 null
			.build();

		mockMvc.perform(MockMvcRequestBuilders.post("/api/comments")
				.contentType(APPLICATION_JSON)
				.header("Authorization", jwt)
				.content(objectMapper.writeValueAsString(commentRequest))
			)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.validation.postId").value("게시글 ID를 입력해주세요."))
			.andDo(print());
	}
}