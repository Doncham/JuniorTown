package org.juniortown.backend.post.service;


import static org.mockito.Mockito.*;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.juniortown.backend.post.dto.request.PostCreateRequest;
import org.juniortown.backend.post.entity.Post;
import org.juniortown.backend.post.repository.PostRepository;
import org.juniortown.backend.user.entity.User;
import org.juniortown.backend.user.exception.UserNotFoundException;
import org.juniortown.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class PostServiceTest {
	@InjectMocks
	private PostService postService;
	@Mock
	private PostRepository postRepository;
	@Mock
	private UserRepository userRepository;

	@BeforeEach
	void clear() {
		postRepository.deleteAll();
	}

	@Test
	@DisplayName("게시글 생성 성공")
	void createPost_success() {
		// given
		PostCreateRequest postCreateRequest = PostCreateRequest.builder()
			.title("테스트 게시물")
			.content("테스트 내용입니다.")
			.build();
		long userId = 1L; // 예시로 사용될 userId

		// when
		when(postRepository.save(any(Post.class))).thenReturn(
			Post.builder()
				.title(postCreateRequest.getTitle())
				.content(postCreateRequest.getContent())
				.user(User.builder().id(userId).build())
				.build()
		);

		when(userRepository.findById(userId)).thenReturn(
			Optional.of(User.builder().id(userId).build())
		);
		postService.create(userId, postCreateRequest);

		// then
		ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
		verify(postRepository).save(captor.capture());
		Post saved = captor.getValue();
		Assertions.assertThat(saved.getTitle()).isEqualTo("테스트 게시물");
		Assertions.assertThat(saved.getContent()).isEqualTo("테스트 내용입니다.");
		Assertions.assertThat(saved.getUser().getId()).isEqualTo(userId);
	}

	@Test
	void createPost_userNotFound_throws() {
		// given
		PostCreateRequest postCreateRequest = PostCreateRequest.builder()
			.title("테스트 게시물")
			.content("테스트 내용입니다.")
			.build();

		// when
		when(userRepository.findById(1L)).thenReturn(Optional.empty());

		// then
		Assertions.assertThatThrownBy(
				() -> postService.create(1L, postCreateRequest))
			.isInstanceOf(UserNotFoundException.class)
			.hasMessage("해당 사용자를 찾을 수 없습니다.");

	}



}