package org.juniortown.backend.comment.dto.response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.juniortown.backend.comment.entity.Comment;

import lombok.Builder;
import lombok.Getter;

@Getter
public class CommentsInPost {
	private final Long commentId;
	private final Long postId;
	private final Long parentId;
	private final Long userId;
	private final String content;
	private final String username;
	private final LocalDateTime createdAt;
	private final LocalDateTime updatedAt;
	private final LocalDateTime deletedAt;
	private final List<CommentsInPost> children = new ArrayList<>();
	@Builder
	public CommentsInPost(Long commentId, Long postId, Long parentId, Long userId, String content, String username, LocalDateTime createdAt,
		LocalDateTime updatedAt, LocalDateTime deletedAt) {
		this.commentId = commentId;
		this.postId = postId;
		this.parentId = parentId;
		this.userId = userId;
		this.content = content;
		this.username = username;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.deletedAt = deletedAt;
	}

	public static CommentsInPost from(Comment comment) {
		return CommentsInPost.builder()
			.commentId(comment.getId())
			.postId(comment.getPost().getId())
			// 근데 이게 꼭 필요한가? userId 쓰는 경우가 생각이 안나
			.userId(comment.getUser().getId())
			.parentId(comment.getParent() != null ? comment.getParent().getId() : null)
			.content(comment.getContent())
			// 이게 원래는 user 테이블을 조회해야하는데 comment에 username이 있으니까
			// 조회를 안해도 되는 사기 스킬
			.username(comment.getUsername())
			.createdAt(comment.getCreatedAt())
			.updatedAt(comment.getUpdatedAt())
			.deletedAt(comment.getDeletedAt())
			.build();
	}
}
