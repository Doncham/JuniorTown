package org.juniortown.backend.post.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostForHome {
	private Long postId;
	private String title;
	private Long userId;
	private String nickname;
	private Long likeCount;
	private Long commentCount;
	private Long readCount;
	private LocalDateTime createdAt;
	@Builder
	public PostForHome(Long postId, String title, Long userId, String nickname, Long likeCount, Long commentCount,
			Long readCount, LocalDateTime createdAt) {
		this.postId = postId;
		this.title = title;
		this.userId = userId;
		this.nickname = nickname;
		this.likeCount = likeCount;
		this.commentCount = commentCount;
		this.readCount = readCount;
		this.createdAt = createdAt;
	}
}
