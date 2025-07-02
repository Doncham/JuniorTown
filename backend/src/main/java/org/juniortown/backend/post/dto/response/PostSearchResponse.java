package org.juniortown.backend.post.dto.response;

import java.time.LocalDateTime;

import org.juniortown.backend.post.entity.Post;

import lombok.Getter;

@Getter
public class PostSearchResponse {
	private Long id;
	private String title;
	private String username;
	private Long userId;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private LocalDateTime deletedAt;

	public PostSearchResponse(Post post) {
		this.id = post.getId();
		this.title = post.getTitle();
		// 이거 null 체크 어지럽네
		this.username = post.getUser().getName();
		this.userId = post.getUser().getId();
		this.createdAt = post.getCreatedAt();
		this.updatedAt = post.getUpdatedAt();
		this.deletedAt = post.getDeletedAt();
	}
}
