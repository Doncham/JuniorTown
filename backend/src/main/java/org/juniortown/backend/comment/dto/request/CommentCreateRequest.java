package org.juniortown.backend.comment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CommentCreateRequest {
	@NotNull(message = "게시글 ID를 입력해주세요.")
	private final Long postId;
	private final Long parentId;
	@NotBlank(message = "댓글 내용을 입력해주세요.")
	private final String content;
}
