package org.juniortown.backend.comment.entity;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.juniortown.backend.comment.exception.AlreadyDeletedCommentException;
import org.juniortown.backend.entity.BaseTimeEntity;
import org.juniortown.backend.post.entity.Post;
import org.juniortown.backend.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Comment extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "parent_id")
	private Comment parent;

	@Lob
	private String content;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "username", nullable = false)
	private String username;

	@ManyToOne
	@JoinColumn(name = "post_id", nullable = false)
	private Post post;

	// 임시로 작성
	@OneToMany(mappedBy = "parent")
	private List<Comment> replies = new ArrayList<>();

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;
	@Builder
	public Comment(Comment parent, String content, User user,String username, Post post) {
		this.parent = parent;
		this.content = content;
		this.user = user;
		this.username = username;
		this.post = post;
	}

	public void softDelete(Clock clock) {
		if(this.deletedAt != null) {
			throw new AlreadyDeletedCommentException();
		}
		this.deletedAt = LocalDateTime.now(clock);
	}

}
