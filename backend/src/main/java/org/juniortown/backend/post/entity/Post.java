package org.juniortown.backend.post.entity;


import java.time.LocalDateTime;
import org.juniortown.backend.entity.BaseTimeEntity;
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
@NoArgsConstructor(access = AccessLevel.PUBLIC)
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

	@Builder
	public Post(String title, String content) {
		this.title = title;
		this.content = content;
	}

	public void edit(String title, String content) {
		this.title = title == null ? this.title : title;
		this.content = content == null ? this.content : content;
	}


}
