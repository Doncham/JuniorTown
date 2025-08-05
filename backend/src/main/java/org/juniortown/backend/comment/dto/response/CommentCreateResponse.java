package org.juniortown.backend.comment.dto.response;

import java.time.LocalDateTime;

import org.juniortown.backend.comment.entity.Comment;

import lombok.Builder;
import lombok.Getter;

@Getter
public class CommentCreateResponse {
	private final Long commentId;
	private final String content;
	private final Long postId;
	private final Long parentId;
	private final Long userId;
	private final String username;
	private final LocalDateTime createdAt;
	@Builder
	public CommentCreateResponse(Long commentId, String content, Long postId, Long parentId, Long userId, String username, LocalDateTime createdAt) {
		this.commentId = commentId;
		this.content = content;
		this.postId = postId;
		this.parentId = parentId;
		this.userId = userId;
		this.username = username;
		this.createdAt = createdAt;
	}
	public static CommentCreateResponse from(Comment comment) {
		return CommentCreateResponse.builder()
			.commentId(comment.getId())
			.content(comment.getContent())
			.postId(comment.getPost().getId())
			.parentId(comment.getParent() != null ? comment.getParent().getId() : null)
			.userId(comment.getUser().getId())
			.username(comment.getUser().getName())
			.createdAt(comment.getCreatedAt())
			.build();
	}
}
