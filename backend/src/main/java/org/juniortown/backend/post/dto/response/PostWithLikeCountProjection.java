package org.juniortown.backend.post.dto.response;

import java.time.LocalDateTime;


public interface PostWithLikeCountProjection {
	Long getId();
	String getTitle();
	String getUsername();
	Long getUserId();
	Long getLikeCount();
	Boolean getIsLiked();
	Long getReadCount();
	LocalDateTime getCreatedAt();
	LocalDateTime getUpdatedAt();
}
