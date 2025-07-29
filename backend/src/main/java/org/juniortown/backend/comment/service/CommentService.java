package org.juniortown.backend.comment.service;

import org.juniortown.backend.comment.dto.request.CommentCreateRequest;
import org.juniortown.backend.comment.dto.response.CommentCreateResponse;
import org.juniortown.backend.comment.entity.Comment;
import org.juniortown.backend.comment.exception.CommentNotFoundException;
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

@Service
@RequiredArgsConstructor
public class CommentService {
	private final CommentRepository commentRepository;
	private final UserRepository userRepository;
	private final PostRepository postRepository;
	@Transactional
	public CommentCreateResponse createComment(Long userId, CommentCreateRequest commentCreateRequest) {
		String content = commentCreateRequest.getContent();
		Long postId = commentCreateRequest.getPostId();
		Long parentId = commentCreateRequest.getParentId();

		 User user = userRepository.findById(userId)
			.orElseThrow(() -> new UserNotFoundException());
		 Post post = postRepository.findById(postId)
			 .orElseThrow(() -> new PostNotFoundException());

		// 최상위 댓글인 경우
		Comment parentComment = null;
		if (parentId != null) {
			parentComment = commentRepository.findById(parentId)
				.orElseThrow(() -> new CommentNotFoundException());
			if(!parentComment.getPost().getId().equals(postId)) {
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
		return CommentCreateResponse.from(saveComment);
	}
}
