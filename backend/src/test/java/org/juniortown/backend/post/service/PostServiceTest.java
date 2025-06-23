package org.juniortown.backend.post.service;


import static org.mockito.Mockito.*;

import java.time.Clock;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.juniortown.backend.post.dto.request.PostCreateRequest;
import org.juniortown.backend.post.entity.Post;
import org.juniortown.backend.post.exception.PostDeletePermissionDeniedException;
import org.juniortown.backend.post.exception.PostNotFoundException;
import org.juniortown.backend.post.exception.PostUpdatePermissionDeniedException;
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
	@Mock
	private Clock clock;

	@Mock
	private Post post;
	@Mock
	private User user;

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
	@DisplayName("게시글 생성 실패 - 사용자 존재하지 않음")
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

	@Test
	@DisplayName("게시글 삭제 성공")
	void deletePost_success() {
		// given
		Long userId = 1L;
		Long postId = 1L;

		when(user.getId()).thenReturn(userId);
		when(post.getUser()).thenReturn(user);

		when(postRepository.findById(postId)).thenReturn(Optional.of(post));

		// when
		postService.delete(postId,userId);

		// then
		verify(post).softDelete(clock);
	}

	@Test
	@DisplayName("게시글 수정 성공")
	void update_post_success() {
		// given
		Long postId = 1L;
		Long userId = 1L;

		PostCreateRequest editRequest = PostCreateRequest.builder()
			.title("Changed Title")
			.content("Changed Content")
			.build();

		// when
		when(postRepository.findById(postId)).thenReturn(Optional.of(post));
		when(post.getUser()).thenReturn(user);
		when(user.getId()).thenReturn(userId);
		postService.update(postId, userId, editRequest);

		// then
		verify(post).update(editRequest, clock);
	}

	@Test
	@DisplayName("게시글 수정 실패 - 게시글이 존재하지 않음")
	void update_post_not_found_failure() {
		// given
		Long postId = 1L;
		Long userId = 1L;

		PostCreateRequest editRequest = PostCreateRequest.builder()
			.title("Changed Title")
			.content("Changed Content")
			.build();

		when(postRepository.findById(postId)).thenReturn(Optional.empty());

		// when & then
		Assertions.assertThatThrownBy(() -> postService.update(postId, userId, editRequest))
			.isInstanceOf(PostNotFoundException.class)
			.hasMessage("해당 게시글을 찾을 수 없습니다.");
	}

	@Test
	@DisplayName("게시글 수정 실패 - 권한 없음")
	void update_post_no_permission_failure() {
		// given
		Long postId = 1L;
		Long userId = 1L;

		PostCreateRequest editRequest = PostCreateRequest.builder()
			.title("Changed Title")
			.content("Changed Content")
			.build();

		// when
		when(postRepository.findById(postId)).thenReturn(Optional.of(post));
		when(post.getUser()).thenReturn(user);
		when(user.getId()).thenReturn(userId + 1);

		// then
		Assertions.assertThatThrownBy(() -> postService.update(postId, userId, editRequest))
			.isInstanceOf(PostUpdatePermissionDeniedException.class)
			.hasMessage("해당 게시글을 수정할 권한이 없습니다.");
	}
}