package org.juniortown.backend.comment.entity;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CommentTest {
	@Test
	@DisplayName("Comment 생성 시 기본 값 설정 확인")
	void softDelete_setsDeletedAt() {
		// given
		Comment comment = Comment.builder()
			.content("Test comment")
			.build();
		Clock fixedClock = Clock.fixed(
			Instant.parse("2025-06-20T10:15:30Z"), ZoneOffset.UTC);
		// when
		comment.softDelete(fixedClock);

		// then
		Assertions.assertThat(comment.getDeletedAt())
			.isEqualTo(LocalDateTime.now(fixedClock));
	}

}