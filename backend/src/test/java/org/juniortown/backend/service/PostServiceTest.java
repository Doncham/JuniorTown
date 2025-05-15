package org.juniortown.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.data.domain.Sort.Direction.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.juniortown.backend.domain.Post;
import org.juniortown.backend.repository.PostRepository;
import org.juniortown.backend.request.PostCreate;
import org.juniortown.backend.request.PostEdit;
import org.juniortown.backend.request.PostSearch;
import org.juniortown.backend.response.PostResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

@SpringBootTest
class PostServiceTest {
	@Autowired
	private PostService postService;
	@Autowired
	private PostRepository postRepository;

	@BeforeEach
	void clear() {
		postRepository.deleteAll();
	}

	@Test
	@DisplayName("글 작성")
	void test1() {
		// given
		PostCreate postCreate = PostCreate.builder()
			.title("제목 입니다.")
			.content("내용 입니다.")
			.build();

		// when
		postService.write(postCreate);

		// then
		assertEquals(1L, postRepository.count());
		Post post = postRepository.findAll().get(0);
		assertEquals("제목 입니다.", post.getTitle());
		assertEquals("내용 입니다.", post.getContent());

	}

	@Test
	@DisplayName("글 1개 조회")
	void test2() {
		// given
		Post requestPost = Post.builder()
			.title("foo")
			.content("bar")
			.build();
		postRepository.save(requestPost);

		// when
		PostResponse response = postService.get(requestPost.getId());

		// then
		assertNotNull(response);
		assertEquals("foo", response.getTitle());
		assertEquals("bar", response.getContent());
	}

	@Test
	@DisplayName("글 여러 조회")
	void test3() {
		// given
		List<Post> requestPosts = IntStream.range(1, 31)
			.mapToObj(i -> {
				return Post.builder()
					.title("호돌맨 제목 " + i)
					.content("반포자이 " + i)
					.build();
			})
			.collect(Collectors.toList());

		postRepository.saveAll(requestPosts);

		PostSearch postSearch = PostSearch.builder()
			.page(1)
			.size(10)
			.build();

		// when
		List<PostResponse> posts = postService.getList(postSearch);

		// then
		assertEquals(10L, posts.size());
		assertEquals("호돌맨 제목 30", posts.get(0).getTitle());
		assertEquals("호돌맨 제목 26", posts.get(4).getTitle());
	}

	@Test
	@DisplayName("글 제목 수정")
	void test4() {
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

		// when
		postService.edit(post.getId(), postEdit);

		// then
		Post changedPost = postRepository.findById(post.getId())
			.orElseThrow(() -> new RuntimeException("글이 존재하지 않습니다. id=" + post.getId()));
		assertEquals("호돌걸", changedPost.getTitle());
		assertEquals("반포자이", changedPost.getContent());

	}

	@Test
	@DisplayName("글 내용 수정")
	void test5() {
		// given
		Post post = Post.builder()
			.title("호돌맨")
			.content("반포자이")
			.build();

		postRepository.save(post);

		PostEdit postEdit = PostEdit.builder()
			.title(null)
			.content("초가집")
			.build();

		// when
		postService.edit(post.getId(), postEdit);

		// then
		Post changedPost = postRepository.findById(post.getId())
			.orElseThrow(() -> new RuntimeException("글이 존재하지 않습니다. id=" + post.getId()));
		assertEquals("호돌맨", changedPost.getTitle());
		assertEquals("초가집", changedPost.getContent());

	}

	@Test
	@DisplayName("게시글 삭제")
	void test6() {
		// given
		Post post = Post.builder()
			.title("호돌맨")
			.content("반포자이")
			.build();

		postRepository.save(post);

		// when
		postService.delete(post.getId());

		// then
		assertEquals(0, postRepository.count());
	}



}