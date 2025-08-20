package org.juniortown.backend.post.dto.request;

import org.juniortown.backend.post.entity.Category;
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
	@NotBlank(message = "카테고리를 입력해주세요.")
	private String category;
	@Builder
	public PostCreateRequest(String title, String content, String category) {
		this.title = title;
		this.content = content;
		this.category = category;
	}

	public Post toEntity(User user) {
		return Post.builder()
			.title(title)
			.content(content)
			.category(Category.valueOf(category))
			.user(user)
			.build();
	}
}
