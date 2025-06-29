package org.juniortown.backend.post.dto.response;

import java.time.LocalDateTime;

import org.juniortown.backend.post.entity.Post;

import lombok.Builder;
import lombok.Getter;

@Getter
public class PostResponse {
	private final Long id;
	private final String title;
	private final String content;
	private Long userId;
	private String userName;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private LocalDateTime deletedAt;

	public PostResponse(Post post) {
		this.id = post.getId();
		this.title = post.getTitle();
		this.content = post.getContent();
		// 이거 null 체크 어지럽네
		this.userId = post.getUser().getId();
		this.userName = post.getUser().getName();
		this.createdAt = post.getCreatedAt();
		this.updatedAt = post.getUpdatedAt();
		this.deletedAt = post.getDeletedAt();
	}
}
