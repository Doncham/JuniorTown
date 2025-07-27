package org.juniortown.backend.post.entity;


import java.time.Clock;
import java.time.LocalDateTime;

import org.juniortown.backend.entity.BaseTimeEntity;
import org.juniortown.backend.post.dto.request.PostCreateRequest;
import org.juniortown.backend.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Post extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String title;
	@Lob
	private String content;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@Column(name = "read_count", nullable = false)
	private Long readCount = 0L;

	@Builder
	public Post(String title, String content, User user) {
		this.title = title;
		this.content = content;
		this.user = user;
	}

	public void softDelete(Clock clock) {
		this.deletedAt = LocalDateTime.now(clock);
	}

	public void update(PostCreateRequest postCreateRequest, Clock clock) {
		this.title = postCreateRequest.getTitle();
		this.content = postCreateRequest.getContent();
	}

	public void addReadCount(Long redisReadCount) {
		this.readCount += redisReadCount;
	}
}
