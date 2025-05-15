package org.juniortown.backend.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Getter
public class Post {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String title;
	@Lob
	private String content;
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
