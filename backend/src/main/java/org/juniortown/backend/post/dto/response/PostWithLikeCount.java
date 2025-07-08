package org.juniortown.backend.post.dto.response;

import java.time.LocalDateTime;

import org.juniortown.backend.post.entity.Post;

import lombok.Getter;

@Getter
public class PostWithLikeCount {
	private Long id;
	private String title;
	private String username;
	private Long userId;
	private Long likeCount;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private LocalDateTime deletedAt;

	public PostWithLikeCount(Post post) {
		this.id = post.getId();
		this.title = post.getTitle();
		this.username = post.getUser().getName();
		this.userId = post.getUser().getId();
		this.createdAt = post.getCreatedAt();
		this.updatedAt = post.getUpdatedAt();
		this.deletedAt = post.getDeletedAt();
	}

	// 생성자 기반 DTO 매핑
	public PostWithLikeCount(Long id, String title, String username, Long userId, Long likeCount, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) {
		this.id = id;
		this.title = title;
		this.username = username;
		this.userId = userId;
		this.likeCount = likeCount;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.deletedAt = deletedAt;
	}
}
