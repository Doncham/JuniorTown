package org.juniortown.backend.controller;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.juniortown.backend.post.dto.PostCreateDTO;
import org.juniortown.backend.post.entity.Post;
import org.juniortown.backend.post.repository.PostRepository;
import org.juniortown.backend.post.dto.PostEdit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class PostControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private PostRepository postRepository;
	@Autowired
	private ObjectMapper objectMapper;

	@BeforeEach
	void clean() {
		postRepository.deleteAll();
	}


	@Test
	@DisplayName("글 작성 요청 시 title 값은 필수다.")
	void test2() throws Exception {
		// given
		PostCreateDTO request = PostCreateDTO.builder()
			.content("내용 입니다.")
			.build();

		String json = objectMapper.writeValueAsString(request);

		// expected
		mockMvc.perform(MockMvcRequestBuilders.post("/posts")
				.contentType(APPLICATION_JSON)
				.content(json)
			)
			.andExpect(jsonPath("$.code").value("400"))
			.andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
			.andExpect(jsonPath("$.validation.title").value("타이틀을 입력해주세요."))
			.andExpect(status().isBadRequest())
			.andDo(print());
	}

	@Test
	@DisplayName("글 작성 요청 시 DB에 값이 저장된다.")
	void test3() throws Exception {
		// given
		PostCreateDTO request = PostCreateDTO.builder()
			.title("제목 입니다.")
			.content("내용 입니다.")
			.build();
		String json = objectMapper.writeValueAsString(request);

		// when
		mockMvc.perform(MockMvcRequestBuilders.post("/posts")
				.contentType(APPLICATION_JSON)
				.content(json)
			)
			.andExpect(status().isOk())
			.andDo(print());

		// then
		Assertions.assertEquals(1L, postRepository.count());

		Post post = postRepository.findAll().get(0);
		assertEquals("제목 입니다.", post.getTitle());
		assertEquals("내용 입니다.", post.getContent());

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
		PostCreateDTO request = PostCreateDTO.builder()
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