package org.juniortown.backend.comment.service;

import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.juniortown.backend.comment.dto.request.CommentCreateRequest;
import org.juniortown.backend.comment.dto.request.CommentUpdateRequest;
import org.juniortown.backend.comment.dto.response.CommentCreateResponse;
import org.juniortown.backend.comment.entity.Comment;
import org.juniortown.backend.comment.exception.AlreadyDeletedCommentException;
import org.juniortown.backend.comment.exception.CommentNotFoundException;
import org.juniortown.backend.comment.exception.DepthLimitTwoException;
import org.juniortown.backend.comment.exception.NoRightForCommentDeleteException;
import org.juniortown.backend.comment.exception.ParentPostMismatchException;
import org.juniortown.backend.comment.repository.CommentRepository;
import org.juniortown.backend.post.entity.Post;
import org.juniortown.backend.post.repository.PostRepository;
import org.juniortown.backend.user.entity.User;
import org.juniortown.backend.user.exception.UserNotFoundException;
import org.juniortown.backend.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class CommentServiceTest {
	@InjectMocks
	private CommentService commentService;
	@Mock
	private CommentRepository commentRepository;
	@Mock
	private UserRepository userRepository;
	@Mock
	private PostRepository postRepository;
	@Mock
	private Clock clock;
	@Mock
	private Post post;
	@Mock
	private User user;
	@Mock
	private Comment comment;
	static final Long USER_ID = 3L;
	static final Long POST_ID = 1L;
	static final Long COMMENT_ID = 5L;
	static final String USERNAME = "testUser";


	@Test
	@DisplayName("댓글 생성 성공 테스트")
	void create_comment_success() {
		// given
		Long parentId = null;
		String content = "This is a comment";
		CommentCreateRequest commentCreateRequest = CommentCreateRequest.builder()
			.content(content)
			.postId(POST_ID)
			.parentId(parentId)
			.build();

		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
		when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));
		when(user.getName()).thenReturn(USERNAME);
		when(user.getId()).thenReturn(USER_ID);
		when(post.getId()).thenReturn(POST_ID);

		when(commentRepository.save(any())).thenReturn(comment);
		when(comment.getId()).thenReturn(COMMENT_ID);
		when(comment.getContent()).thenReturn(content);
		when(comment.getPost()).thenReturn(post);
		when(comment.getParent()).thenReturn(null);
		when(comment.getUser()).thenReturn(user);

		// when
		CommentCreateResponse response = commentService.createComment(USER_ID, commentCreateRequest);

		// then
		verify(commentRepository).save(any());
		Assertions.assertThat(response.getContent()).isEqualTo(content);
		Assertions.assertThat(response.getPostId()).isEqualTo(POST_ID);
		Assertions.assertThat(response.getParentId()).isNull();
		Assertions.assertThat(response.getUserId()).isEqualTo(USER_ID);
		Assertions.assertThat(response.getUsername()).isEqualTo(user.getName());
	}

	@Test
	@DisplayName("대댓글 생성 성공 테스트")
	void create_child_comment_success() {
		// given
		Long parentId = 2L;
		String content = "This is a comment";
		CommentCreateRequest commentCreateRequest = CommentCreateRequest.builder()
			.content(content)
			.postId(POST_ID)
			.parentId(parentId)
			.build();

		Comment parentComment = mock(Comment.class);
		when(parentComment.getId()).thenReturn(parentId);
		when(commentRepository.findById(parentId)).thenReturn(Optional.of(parentComment));
		when(parentComment.getPost()).thenReturn(post);

		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
		when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));
		when(user.getName()).thenReturn(USERNAME);
		when(user.getId()).thenReturn(USER_ID);
		when(post.getId()).thenReturn(POST_ID);

		when(commentRepository.save(any())).thenReturn(comment);
		when(comment.getId()).thenReturn(COMMENT_ID);
		when(comment.getContent()).thenReturn(content);
		when(comment.getPost()).thenReturn(post);
		when(comment.getParent()).thenReturn(null);
		when(comment.getUser()).thenReturn(user);
		when(comment.getParent()).thenReturn(parentComment);

		// when
		CommentCreateResponse response = commentService.createComment(USER_ID, commentCreateRequest);

		// then
		verify(commentRepository).save(any());
		Assertions.assertThat(response.getContent()).isEqualTo(content);
		Assertions.assertThat(response.getPostId()).isEqualTo(POST_ID);
		Assertions.assertThat(response.getUserId()).isEqualTo(USER_ID);
		Assertions.assertThat(response.getUsername()).isEqualTo(user.getName());
		Assertions.assertThat(response.getParentId()).isEqualTo(parentId);
	}

	@Test
	@DisplayName("대댓글 생성 실패 테스트 - 부모 댓글이 다른 게시글에 속함")
	void create_child_comment_fail_by_parent_child_comments_have_different_post() {
		// given
		Long parentId = 2L;
		String content = "This is a comment";
		CommentCreateRequest commentCreateRequest = CommentCreateRequest.builder()
			.content(content)
			.postId(POST_ID)
			.parentId(parentId)
			.build();

		Comment parentComment = mock(Comment.class);
		Post otherPost = mock(Post.class);
		when(commentRepository.findById(parentId)).thenReturn(Optional.of(parentComment));
		when(parentComment.getPost()).thenReturn(otherPost);

		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
		when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));

		// when, then
		verify(commentRepository, never()).save(any(Comment.class));
		// 예외 터진거 확인은 어떻게 해?
		Assertions.assertThatThrownBy(() -> commentService.createComment(USER_ID, commentCreateRequest))
			.isInstanceOf(ParentPostMismatchException.class)
			.hasMessage("부모 댓글의 게시글과 대댓글이 속한 게시글이 일치하지 않습니다.");
		verify(commentRepository, never()).save(any(Comment.class));
	}
	@Test
	@DisplayName("대댓글 생성 실패 테스트 - 부모 댓글이 대댓글인 경우 depth-2를 위반")
	void create_child_comment_fail_by_parent_comment_has_parent_comment() {
		// given
		Long parentId = 2L;

		String content = "This is a comment";
		CommentCreateRequest commentCreateRequest = CommentCreateRequest.builder()
			.content(content)
			.postId(POST_ID)
			.parentId(parentId)
			.build();

		Comment parentComment = mock(Comment.class);
		when(commentRepository.findById(parentId)).thenReturn(Optional.of(parentComment));
		when(parentComment.getParent()).thenReturn(mock(Comment.class)); // 부모 댓글이 대댓글인 경우

		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
		when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));


		// when, then
		verify(commentRepository, never()).save(any(Comment.class));
		Assertions.assertThatThrownBy(() -> commentService.createComment(USER_ID, commentCreateRequest))
			.isInstanceOf(DepthLimitTwoException.class)
			.hasMessage(DepthLimitTwoException.MESSAGE);
		verify(commentRepository, never()).save(any(Comment.class));
	}

	@Test
	@DisplayName("대댓글 생성 실패 테스트 - 부모 댓글이 알고보니 삭제됨")
	void create_child_comment_fail_by_parent_comment_is_deleted() {
		// given
		Long parentId = 2L;

		String content = "This is a comment";
		CommentCreateRequest commentCreateRequest = CommentCreateRequest.builder()
			.content(content)
			.postId(POST_ID)
			.parentId(parentId)
			.build();

		Comment parentComment = mock(Comment.class);
		when(commentRepository.findById(parentId)).thenReturn(Optional.of(parentComment));
		when(parentComment.getDeletedAt()).thenReturn(LocalDateTime.now()); // 부모 댓글이 대댓글인 경우

		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
		when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));


		// when, then
		verify(commentRepository, never()).save(any(Comment.class));
		Assertions.assertThatThrownBy(() -> commentService.createComment(USER_ID, commentCreateRequest))
			.isInstanceOf(AlreadyDeletedCommentException.class)
			.hasMessage(AlreadyDeletedCommentException.MESSAGE);
		verify(commentRepository, never()).save(any(Comment.class));
	}

	@Test
	@DisplayName("댓글 생성 실패 테스트 - 사용자 존재하지 않음")
	void create_child_comment_fail_by_no_user() {
		// given
		Long parentId = 2L;
		String content = "This is a comment";
		CommentCreateRequest commentCreateRequest = CommentCreateRequest.builder()
			.content(content)
			.postId(POST_ID)
			.parentId(parentId)
			.build();

		when(userRepository.findById(USER_ID)).thenReturn(Optional.ofNullable(null));

		// when, then
		Assertions.assertThatThrownBy(() -> commentService.createComment(USER_ID, commentCreateRequest))
			.isInstanceOf(UserNotFoundException.class)
			.hasMessage("해당 사용자를 찾을 수 없습니다.");
		verify(commentRepository, never()).save(any(Comment.class));
	}
	@Test
	@DisplayName("댓글 생성 실패 테스트 - 게시글 존재하지 않음")
	void create_child_comment_fail_by_no_post() {
		// given
		Long parentId = 2L;
		String content = "This is a comment";
		CommentCreateRequest commentCreateRequest = CommentCreateRequest.builder()
			.content(content)
			.postId(POST_ID)
			.parentId(parentId)
			.build();

		when(userRepository.findById(USER_ID)).thenReturn(Optional.ofNullable(null));

		// when, then
		Assertions.assertThatThrownBy(() -> commentService.createComment(USER_ID, commentCreateRequest))
			.isInstanceOf(UserNotFoundException.class)
			.hasMessage("해당 사용자를 찾을 수 없습니다.");
		verify(commentRepository, never()).save(any(Comment.class));
	}

	@Test
	@DisplayName("댓글 생성 실패 테스트 - 부모 댓글이 존재하지 않음")
	void create_child_comment_fail_by_parent_post_not_exist() {
		// given
		Long parentId = 2L;
		String content = "This is a comment";
		CommentCreateRequest commentCreateRequest = CommentCreateRequest.builder()
			.content(content)
			.postId(POST_ID)
			.parentId(parentId)
			.build();

		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
		when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));
		when(commentRepository.findById(parentId)).thenReturn(Optional.empty());

		// when, then
		Assertions.assertThatThrownBy(() -> commentService.createComment(USER_ID, commentCreateRequest))
			.isInstanceOf(CommentNotFoundException.class)
			.hasMessage("해당 댓글을 찾을 수 없습니다.");
		verify(commentRepository, never()).save(any(Comment.class));
	}

	@Test
	@DisplayName("댓글 삭제 성공 테스트")
	void delete_comment_success() {
		// given
		Long commentId = 1L;
		Long userId = 2L;

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
		when(comment.getUser()).thenReturn(user);
		when(user.getId()).thenReturn(userId);

		// when
		commentService.deleteComment(userId, commentId);

		// then
		verify(comment).softDelete(any(Clock.class));
	}
	@Test
	@DisplayName("댓글 삭제 실패 테스트 - 사용자가 존재하지 않음")
	void delete_comment_fail_by_no_user() {
		// given
		Long commentId = 1L;
		Long userId = 2L;

		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		// when, then
		Assertions.assertThatThrownBy(() -> commentService.deleteComment(userId, commentId))
			.isInstanceOf(UserNotFoundException.class)
			.hasMessage(UserNotFoundException.MESSAGE);
		verify(commentRepository, never()).findById(commentId);
	}

	@Test
	@DisplayName("댓글 삭제 실패 테스트 - 게시글이 존재하지 않음")
	void delete_comment_fail_by_no_comment() {
		// given
		Long commentId = 1L;
		Long userId = 2L;

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

		// when, then
		Assertions.assertThatThrownBy(() -> commentService.deleteComment(userId, commentId))
			.isInstanceOf(CommentNotFoundException.class)
			.hasMessage(CommentNotFoundException.MESSAGE);
		verify(comment, never()).softDelete(any(Clock.class));
	}
	@Test
	@DisplayName("댓글 삭제 실패 테스트 - 사용자가 댓글을 작성한 사용자가 아님")
	void delete_comment_fail_by_no_right_for_delete() {
		// given
		Long commentId = 1L;
		Long userId = 2L;

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
		when(comment.getUser()).thenReturn(mock(User.class));
		when(comment.getUser().getId()).thenReturn(3L); // 다른 사용자

		// when, then
		Assertions.assertThatThrownBy(() -> commentService.deleteComment(userId, commentId))
			.isInstanceOf(NoRightForCommentDeleteException.class)
			.hasMessage(NoRightForCommentDeleteException.MESSAGE);
		verify(comment, never()).softDelete(any(Clock.class));
	}
	@Test
	@DisplayName("댓글 수정 성공 테스트")
	void update_comment_success() {
		// given
		Long commentId = 1L;
		Long userId = 2L;
		String newContent = "Updated comment content";
		CommentUpdateRequest commentUpdateRequest = CommentUpdateRequest.builder()
			.content(newContent)
			.build();

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
		when(comment.getUser()).thenReturn(user);
		when(user.getId()).thenReturn(userId);

		// when
		commentService.updateComment(userId, commentId, commentUpdateRequest);

		// then
		verify(comment).update(any(CommentUpdateRequest.class), any(Clock.class));
	}
	@Test
	@DisplayName("댓글 수정 실패 테스트 - 사용자가 존재하지 않음")
	void update_comment_fail_by_no_user() {
		// given
		Long commentId = 1L;
		Long userId = 2L;
		CommentUpdateRequest commentUpdateRequest = CommentUpdateRequest.builder()
			.content("Updated comment content")
			.build();

		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		// when, then
		Assertions.assertThatThrownBy(() -> commentService.updateComment(userId, commentId, commentUpdateRequest))
			.isInstanceOf(UserNotFoundException.class)
			.hasMessage(UserNotFoundException.MESSAGE);
		verify(commentRepository, never()).findById(commentId);
	}

	@Test
	@DisplayName("댓글 수정 실패 테스트 - 댓글이 존재하지 않음")
	void update_comment_fail_by_no_comment() {
		// given
		Long commentId = 1L;
		Long userId = 2L;
		CommentUpdateRequest commentUpdateRequest = CommentUpdateRequest.builder()
			.content("Updated comment content")
			.build();

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

		// when, then
		Assertions.assertThatThrownBy(() -> commentService.updateComment(userId, commentId, commentUpdateRequest))
			.isInstanceOf(CommentNotFoundException.class)
			.hasMessage(CommentNotFoundException.MESSAGE);
		verify(comment, never()).update(any(CommentUpdateRequest.class), any(Clock.class));
	}
	@Test
	@DisplayName("댓글 수정 실패 테스트 - 사용자가 댓글 작성자가 아님")
	void update_comment_fail_by_no_right_for_update() {
		// given
		Long commentId = 1L;
		Long userId = 2L;
		CommentUpdateRequest commentUpdateRequest = CommentUpdateRequest.builder()
			.content("Updated comment content")
			.build();

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
		when(comment.getUser()).thenReturn(mock(User.class));
		when(comment.getUser().getId()).thenReturn(3L); // 다른 사용자

		// when, then
		Assertions.assertThatThrownBy(() -> commentService.updateComment(userId, commentId, commentUpdateRequest))
			.isInstanceOf(NoRightForCommentDeleteException.class)
			.hasMessage(NoRightForCommentDeleteException.MESSAGE);
		verify(comment, never()).update(any(CommentUpdateRequest.class), any(Clock.class));
	}
}

