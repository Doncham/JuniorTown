package org.juniortown.backend.like.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LikeResponse {
	private Long userId;
	private Long postId;
	private Boolean isLiked;
	@Builder
	public LikeResponse(Long userId, Long postId, Boolean isLiked) {
		this.userId = userId;
		this.postId = postId;
		this.isLiked = isLiked;
	}
}
