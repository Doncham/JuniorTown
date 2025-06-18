package org.juniortown.backend.post.service;


import static org.mockito.Mockito.*;

import org.assertj.core.api.Assertions;
import org.juniortown.backend.post.dto.PostCreateDTO;
import org.juniortown.backend.post.entity.Post;
import org.juniortown.backend.post.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class PostServiceTest {
	@InjectMocks
	private  PostService postService;
	@Mock
	private PostRepository postRepository;

	@BeforeEach
	void clear() {
		postRepository.deleteAll();
	}

	@Test
	@DisplayName("게시글 생성 성공")
	void createPost_success() {
		// given
		PostCreateDTO postCreateDTO = PostCreateDTO.builder()
			.title("테스트 게시물")
			.content("테스트 내용입니다.")
			.build();

		when(postRepository.save(any(Post.class))).thenReturn(
			Post.builder()
				.title(postCreateDTO.getTitle())
				.content(postCreateDTO.getContent())
				.build()
		);

		// when
		postService.create(postCreateDTO);


		// then
		verify(postRepository).save(any(Post.class));
		Assertions.assertThat(postCreateDTO.getTitle()).isEqualTo("테스트 게시물");
		Assertions.assertThat(postCreateDTO.getContent()).isEqualTo("테스트 내용입니다.");
	}



}