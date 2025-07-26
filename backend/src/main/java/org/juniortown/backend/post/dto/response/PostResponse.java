package org.juniortown.backend.post.dto.response;

import java.time.LocalDateTime;

import org.juniortown.backend.post.entity.Post;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
public class PostResponse {
	private final Long id;
	private final String title;
	private final String content;
	private final Long userId;
	private final String userName;
	private final Long likeCount;
	private Long readCount;
	private Boolean isLiked;
	private final LocalDateTime createdAt;
	private final LocalDateTime updatedAt;
	private final LocalDateTime deletedAt;

	@Builder
	public PostResponse(Long id, String title, String content, Long userId, String userName, Long likeCount,
			Boolean isLiked, Long readCount, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) {
		this.id = id;
		this.title = title;
		this.content = content;
		this.userId = userId;
		this.userName = userName;
		this.likeCount = likeCount;
		this.isLiked = isLiked;
		this.readCount = readCount;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.deletedAt = deletedAt;
	}
	public static PostResponse from(Post post) {
		return PostResponse.builder()
			.id(post.getId())
			.title(post.getTitle())
			.content(post.getContent())
			.userId(post.getUser().getId())
			.userName(post.getUser().getName())
			.readCount(post.getReadCount())
			.createdAt(post.getCreatedAt())
			.updatedAt(post.getUpdatedAt())
			.deletedAt(post.getDeletedAt())
			.build();
	}
}
