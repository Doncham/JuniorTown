package org.juniortown.backend.controller;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.juniortown.backend.post.dto.request.PostCreateRequest;
import org.juniortown.backend.post.entity.Post;
import org.juniortown.backend.post.repository.PostRepository;
import org.juniortown.backend.post.dto.PostEdit;
import org.juniortown.backend.user.dto.LoginDTO;
import org.juniortown.backend.user.jwt.JWTUtil;
import org.juniortown.backend.user.request.SignUpDTO;
import org.juniortown.backend.user.service.AuthService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // 클래스 단위로 테스트 인스턴스를 생성한다.
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
class PostControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private PostRepository postRepository;
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

	@BeforeAll
	public void init() throws Exception {
		SignUpDTO signUpDTO = SignUpDTO.builder()
			.email("test@naver.com")
			.password("1234")
			.username("테스터")
			.build();

		LoginDTO loginDTO = LoginDTO.builder()
			.email(signUpDTO.getEmail())
			.password(signUpDTO.getPassword())
			.build();
		authService.signUp(signUpDTO);
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
	@DisplayName("회원 가입된 유저만 글 작성을 요청할 수 있다.")
	// 물론 서비스에서 예외를 터뜨리기는 하지만 JWT 관련 예외라서 여기서 검증함.
	void only_signupUser_can_make_post() throws Exception {
		// given
		PostCreateRequest request = PostCreateRequest.builder()
			.title("제목입니다.")
			.content("내용입니다.")
			.build();

		String json = objectMapper.writeValueAsString(request);

		String ghostToken = jwtUtil.createJwt(100L, "test@gmail.com", "ROLE_USER", 1000L);

		// expected
		mockMvc.perform(MockMvcRequestBuilders.post("/api/posts")
				.contentType(APPLICATION_JSON)
				.content(json)
				.header("Authorization", "Bearer " + ghostToken)
			)
			.andExpect(jsonPath("$.code").value("404"))
			.andExpect(jsonPath("$.message").value("해당 사용자를 찾을 수 없습니다."))
			.andExpect(status().isNotFound())
			.andDo(print());
	}

	@Test
	@DisplayName("글 작성 요청 시 content 값은 필수다.")
	void need_content_when_create_post() throws Exception {
		// given
		PostCreateRequest request = PostCreateRequest.builder()
			.title("제목 입니다.")
			.build();

		String json = objectMapper.writeValueAsString(request);

		// expected
		mockMvc.perform(MockMvcRequestBuilders.post("/api/posts")
				.contentType(APPLICATION_JSON)
				.content(json)
				.header("Authorization", jwt)
			)
			.andExpect(jsonPath("$.code").value("400"))
			.andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
			.andExpect(jsonPath("$.validation.content").value("컨텐츠를 입력해주세요."))
			.andExpect(status().isBadRequest())
			.andDo(print());
	}

	@Test
	@DisplayName("글 작성 요청 시 DB에 값이 저장된다.")
	void post_should_be_in_DB() throws Exception {
		// given
		PostCreateRequest request = PostCreateRequest.builder()
			.title("제목 입니다.")
			.content("내용 입니다.")
			.build();
		String json = objectMapper.writeValueAsString(request);

		// when
		mockMvc.perform(MockMvcRequestBuilders.post("/api/posts")
				.contentType(APPLICATION_JSON)
				.content(json)
				.header("Authorization", jwt)
			)
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.title").value(request.getTitle()))
			.andExpect(jsonPath("$.content").value(request.getContent()))
			.andExpect(jsonPath("$.userId").value(jwtUtil.getUserId(jwt.split(" ")[1])))
			.andDo(print());

		// then
		Assertions.assertEquals(1L, postRepository.count());

		Post post = postRepository.findAll().get(0);
		assertEquals("제목 입니다.", post.getTitle());
		assertEquals("내용 입니다.", post.getContent());

	}

	@Test
	@DisplayName("글 작성 요청 시 DB에 값이 저장된다.")
	void post_create_success() throws Exception {
		// given
		PostCreateRequest request = PostCreateRequest.builder()
			.title("제목 입니다.")
			.content("내용 입니다.")
			.build();
		String json = objectMapper.writeValueAsString(request);

		// when
		mockMvc.perform(MockMvcRequestBuilders.post("/api/posts")
				.contentType(APPLICATION_JSON)
				.content(json)
				.header("Authorization", jwt)
			)
			.andExpect(status().isCreated())
			.andDo(print());
	}



	@Test
	@DisplayName("글 1개 조회")
	void test4() throws Exception {
		// given
		Post post = Post.builder()
			.title("123456789012345")
			.content("bar")
			.build();
		postRepository.save(post);

		// when + then -> expected
		mockMvc.perform(MockMvcRequestBuilders.get("/posts/{postId}", post.getId())
				.contentType(APPLICATION_JSON)
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(post.getId()))
			.andExpect(jsonPath("$.title").value("1234567890"))
			.andExpect(jsonPath("$.content").value(post.getContent()))
			.andDo(print());


	}
	@Test
	@DisplayName("글 여러개 조회")
	void test5() throws Exception {
		// given
		List<Post> requestPosts = IntStream.range(1, 20)
			.mapToObj(i -> {
				return Post.builder()
					.title("foo" + i)
					.content("bar" + i)
					.build();
			})
			.collect(Collectors.toList());

		postRepository.saveAll(requestPosts);

		// when + then -> expected
		mockMvc.perform(MockMvcRequestBuilders.get("/posts?page=1&size=10")
				.contentType(APPLICATION_JSON)
			)
			.andExpect(jsonPath("$.length()", is(10)))
			//.andExpect(jsonPath("$[0].id", is(19)))
			.andExpect(jsonPath("$[0].title", is("foo19")))
			.andExpect(jsonPath("$[0].content", is("bar19")))

			.andExpect(status().isOk())
			.andDo(print());

		// then
	}

	@Test
	@DisplayName("글 제목 수정")
	void test6() throws Exception {
		// given
		Post post = Post.builder()
			.title("호돌맨")
			.content("반포자이")
			.build();

		postRepository.save(post);

		PostEdit postEdit = PostEdit.builder()
			.title("호돌걸")
			.content("반포자이")
			.build();

		// when + then -> expected
		mockMvc.perform(MockMvcRequestBuilders.patch("/posts/{postId}", post.getId())
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(postEdit))
			)
			.andExpect(status().isOk())
			.andDo(print());
	}

	@Test
	@DisplayName("게시글 삭제")
	void test8() throws Exception {
		// given
		Post post = Post.builder()
			.title("호돌맨")
			.content("반포자이")
			.build();

		postRepository.save(post);

		// expected
		mockMvc.perform(MockMvcRequestBuilders.delete("/posts/{postId}", post.getId())
				.contentType(APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(print());
	}

	@Test
	@DisplayName("존재하지 않는 게시글 조회")
	void test9() throws Exception {
		// expected
		mockMvc.perform(MockMvcRequestBuilders.get("/posts/{postId}", 1L)
				.contentType(APPLICATION_JSON))
			.andExpect(status().isNotFound())
			.andDo(print());
	}
	@Test
	@DisplayName("존재하지 않는 게시글 수정")
	void test10() throws Exception {
		// expected
		PostEdit postEdit = PostEdit.builder()
			.title("호돌걸")
			.content("반포자이")
			.build();

		// expected
		mockMvc.perform(MockMvcRequestBuilders.patch("/posts/{postId}", 1L)
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(postEdit))
			)
			.andExpect(status().isNotFound())
			.andDo(print());
	}
	@Test
	@DisplayName("게시글 작성 시 제목에 '바보'는 포함될 수 없다.")
	void test11() throws Exception {
		// expected
		PostCreateRequest request = PostCreateRequest.builder()
			.title("나는 바보입니다.")
			.content("반포자이")
			.build();

		// expected
		mockMvc.perform(MockMvcRequestBuilders.post("/posts")
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isBadRequest())
			.andDo(print());
	}
}