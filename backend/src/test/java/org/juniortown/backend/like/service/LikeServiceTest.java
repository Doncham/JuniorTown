package org.juniortown.backend.like.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.juniortown.backend.like.dto.response.LikeResponse;
import org.juniortown.backend.like.entity.Like;
import org.juniortown.backend.like.exception.LikeFailureException;
import org.juniortown.backend.like.repository.LikeRepository;
import org.juniortown.backend.post.entity.Post;
import org.juniortown.backend.post.repository.PostRepository;
import org.juniortown.backend.user.entity.User;
import org.juniortown.backend.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.parameters.P;
import org.springframework.test.context.ActiveProfiles;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class LikeServiceTest {
	@InjectMocks
	private LikeService likeService;
	@Mock
	private UserRepository userRepository;
	@Mock
	private PostRepository postRepository;
	@Mock
	private LikeRepository likeRepository;
	@Mock
	private Post post;
	@Mock
	private User user;
	@Mock
	private Like like;

	@Test
	@DisplayName("좋아요 테스트 - 성공")
	void like_success_test() {
		// given
		Long userId = 1L;
		Long postId = 1L;

		when(likeRepository.findByUserIdAndPostId(userId, postId)).thenReturn(Optional.empty());
		when(userRepository.getReferenceById(userId)).thenReturn(user);
		when(postRepository.getReferenceById(postId)).thenReturn(post);
		// when
		LikeResponse likeResponse = likeService.likePost(userId, postId);

		// then
		verify(likeRepository).save(any(Like.class));
		assertThat(likeResponse.getUserId()).isEqualTo(userId);
		assertThat(likeResponse.getPostId()).isEqualTo(postId);
		assertThat(likeResponse.getIsLiked()).isTrue();
	}

	@Test
	@DisplayName("좋아요 취소 - 이미 좋아요한 경우")
	void unlike_success_test() {
		// given
		Long userId = 1L;
		Long postId = 1L;

		when(likeRepository.findByUserIdAndPostId(userId, postId)).thenReturn(Optional.of(like));
		when(like.getId()).thenReturn(1L);
		// when
		LikeResponse likeResponse = likeService.likePost(userId, postId);

		// then
		verify(likeRepository).deleteById(any(Long.class));
		assertThat(likeResponse.getUserId()).isEqualTo(userId);
		assertThat(likeResponse.getPostId()).isEqualTo(postId);
		assertThat(likeResponse.getIsLiked()).isFalse();
	}

	@Test
	@DisplayName("좋아요 실패 - 예외 발생")
	void like_failure_test() {
		// given
		Long userId = 1L;
		Long postId = 1L;

		when(userRepository.getReferenceById(userId)).thenReturn(user);
		when(postRepository.getReferenceById(postId)).thenReturn(post);
		when(likeRepository.save(any(Like.class)))
			.thenThrow((new RuntimeException("게시글 삭제됐는데? DB 예외 펑!")));

		// when,then
		assertThatThrownBy(() -> likeService.likePost(userId, postId))
			.isInstanceOf(LikeFailureException.class)
			.hasMessageContaining(LikeFailureException.MESSAGE);
	}

}