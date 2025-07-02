package org.juniortown.backend.post.dto.response;

import java.time.LocalDateTime;

import org.juniortown.backend.post.entity.Post;

import lombok.Getter;

@Getter
public class PostResponse {
	private final Long id;
	private final String title;
	private final String content;
	private final Long userId;
	private final String userName;
	private final LocalDateTime createdAt;
	private final LocalDateTime updatedAt;
	private final LocalDateTime deletedAt;

	public PostResponse(Post post) {
		this.id = post.getId();
		this.title = post.getTitle();
		this.content = post.getContent();
		this.userId = post.getUser().getId();
		this.userName = post.getUser().getName();
		this.createdAt = post.getCreatedAt();
		this.updatedAt = post.getUpdatedAt();
		this.deletedAt = post.getDeletedAt();
	}
}
