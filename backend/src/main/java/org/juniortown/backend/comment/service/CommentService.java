package org.juniortown.backend.comment.service;

import java.time.Clock;

import org.juniortown.backend.comment.dto.request.CommentCreateRequest;
import org.juniortown.backend.comment.dto.request.CommentUpdateRequest;
import org.juniortown.backend.comment.dto.response.CommentCreateResponse;
import org.juniortown.backend.comment.entity.Comment;
import org.juniortown.backend.comment.exception.AlreadyDeletedCommentException;
import org.juniortown.backend.comment.exception.CircularReferenceException;
import org.juniortown.backend.comment.exception.CommentNotFoundException;
import org.juniortown.backend.comment.exception.DepthLimitTwoException;
import org.juniortown.backend.comment.exception.NoRightForCommentDeleteException;
import org.juniortown.backend.comment.exception.NoRightForCommentUpdateException;
import org.juniortown.backend.comment.exception.ParentPostMismatchException;
import org.juniortown.backend.comment.repository.CommentRepository;
import org.juniortown.backend.post.entity.Post;
import org.juniortown.backend.post.exception.PostNotFoundException;
import org.juniortown.backend.post.repository.PostRepository;
import org.juniortown.backend.user.entity.User;
import org.juniortown.backend.user.exception.UserNotFoundException;
import org.juniortown.backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CommentService {
	private final CommentRepository commentRepository;
	private final UserRepository userRepository;
	private final PostRepository postRepository;
	private final Clock clock;

	public CommentCreateResponse createComment(Long userId, CommentCreateRequest commentCreateRequest) {
		String content = commentCreateRequest.getContent();
		Long postId = commentCreateRequest.getPostId();
		Long parentId = commentCreateRequest.getParentId();

		 User user = userRepository.findById(userId)
			.orElseThrow(() -> {
				log.info("User not found with id: {}", userId);
				return new UserNotFoundException();
			});
		 Post post = postRepository.findById(postId)
			 .orElseThrow(() -> {
				log.info("Post not found with id: {}", postId);
				 return new PostNotFoundException();
			 });

		// 최상위 댓글인 경우
		Comment parentComment = null;
		if (parentId != null) {
			parentComment = commentRepository.findById(parentId)
				.orElseThrow(() -> {
					log.info("Parent comment not found with id: {}", parentId);
					return new CommentNotFoundException();
				});
			if (parentComment.getDeletedAt() != null) {
				log.info("Parent comment with id {} is already deleted.", parentId);
				throw new AlreadyDeletedCommentException();
			}
			if(parentComment.getParent() != null) {
				log.info("depth는 2로 제한됩니다. 현재 parentComment의 parent가 존재합니다.");
				throw new DepthLimitTwoException();
			}
			if(!parentComment.getPost().getId().equals(postId)) {
				log.info("comment의 postId {}가 post ID {}와 다름.", parentComment.getPost().getId(), postId);
				throw new ParentPostMismatchException();
			}
		}


		Comment comment = Comment.builder()
			.content(content)
			.user(user)
			.username(user.getName())
			.post(post)
			.parent(parentComment)
			.build();

		Comment saveComment = commentRepository.save(comment);
		// 순환 참조 방지 로직
		validateNoCircularReference(parentComment, saveComment.getId());
		return CommentCreateResponse.from(saveComment);
	}

	public void deleteComment(Long userId, Long commentId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> {
				log.info("User not found with id: {}", userId);
				return new UserNotFoundException();
			});
		Comment comment = commentRepository.findById(commentId)
			.orElseThrow(() -> {
				log.info("Comment not found with id: {}", commentId);
				return new CommentNotFoundException();
			});

		if (!comment.getUser().getId().equals(user.getId())) {
			log.info("User {} does not have permission to delete comment {}", userId, commentId);
			throw new NoRightForCommentDeleteException();
		}

		comment.softDelete(clock);
	}

	public void updateComment(Long userId, Long commentId, CommentUpdateRequest commentUpdateRequest) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> {
				log.info("User not found with id: {}", userId);
				return new UserNotFoundException();
			});
		Comment comment = commentRepository.findById(commentId)
			.orElseThrow(() -> {
				log.info("Comment not found with id: {}", commentId);
				return new CommentNotFoundException();
			});

		if (!comment.getUser().getId().equals(user.getId())) {
			log.info("User {} does not have permission to update comment {}", userId, commentId);
			throw new NoRightForCommentUpdateException();
		}
		comment.update(commentUpdateRequest, clock);
		// 순환 참조 방지 로직
		validateNoCircularReference(comment.getParent(), commentId);
	}
	private void validateNoCircularReference(Comment parent, Long childId) {
		Comment current = parent;
		while (current != null) {
			if (current.getId().equals(childId)) {
				throw new CircularReferenceException();
			}
			current = current.getParent();
		}
	}

}
