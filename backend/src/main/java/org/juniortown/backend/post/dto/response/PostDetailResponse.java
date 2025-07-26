package org.juniortown.backend.post.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
public class PostDetailResponse {
	private Long id;
	private String title;
	private String content;
	private Long userId;
	private String userName;
	private Long likeCount;
	private Boolean isLiked;
	private Long readCount;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private LocalDateTime deletedAt;

	@Builder
	public PostDetailResponse(Long id, String title, String content, Long userId, String userName, Long likeCount,
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
	public static PostDetailResponse from(PostResponse postResponse) {
		return PostDetailResponse.builder()
			.id(postResponse.getId())
			.title(postResponse.getTitle())
			.content(postResponse.getContent())
			.userId(postResponse.getUserId())
			.userName(postResponse.getUserName())
			.likeCount(postResponse.getLikeCount())
			.isLiked(postResponse.getIsLiked())
			.readCount(postResponse.getReadCount())
			.createdAt(postResponse.getCreatedAt())
			.updatedAt(postResponse.getUpdatedAt())
			.deletedAt(postResponse.getDeletedAt())
			.build();
	}
	public void addReadCount(Long redisReadCount) {
		this.readCount += redisReadCount;
	}
}
