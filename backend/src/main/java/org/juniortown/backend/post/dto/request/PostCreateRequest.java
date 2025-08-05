package org.juniortown.backend.post.dto.request;

import org.juniortown.backend.post.entity.Post;
import org.juniortown.backend.user.entity.User;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter //??
@Getter
@ToString
public class PostCreateRequest {
	@NotBlank(message = "타이틀을 입력해주세요.")
	private String title;
	@NotBlank(message = "컨텐츠를 입력해주세요.")
	private String content;
	@Builder
	public PostCreateRequest(String title, String content) {
		this.title = title;
		this.content = content;
	}

	public Post toEntity(User user) {
		return Post.builder()
			.title(title)
			.content(content)
			.user(user)
			.build();
	}
}
