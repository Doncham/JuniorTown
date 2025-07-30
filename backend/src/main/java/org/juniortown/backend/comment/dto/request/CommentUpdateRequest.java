package org.juniortown.backend.comment.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CommentUpdateRequest {
	@NotBlank(message = "댓글 내용을 입력해주세요.")
	private final String content;
	@Builder
	public CommentUpdateRequest(String content) {
		this.content = content;
	}
}
