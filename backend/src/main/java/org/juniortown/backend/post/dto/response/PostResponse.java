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
	//private final Long likeCount;
	private Long readCount;
	private final LocalDateTime createdAt;
	private final LocalDateTime updatedAt;
	private final LocalDateTime deletedAt;

	public PostResponse(Post post) {
		this.id = post.getId();
		this.title = post.getTitle();
		this.content = post.getContent();
		this.userId = post.getUser().getId();
		this.userName = post.getUser().getName();
		// this.likeCount = post.getLikeCount();
		// 좋아요 수 집계함수로 컬럼으로 만들까?
		this.readCount = post.getReadCount();
		this.createdAt = post.getCreatedAt();
		this.updatedAt = post.getUpdatedAt();
		this.deletedAt = post.getDeletedAt();
	}

	public void addReadCount(Long redisRedaCount) {
		this.readCount += redisRedaCount;
	}
}
