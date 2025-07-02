package org.juniortown.backend.post.entity;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.Time;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.juniortown.backend.post.dto.request.PostCreateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PostTest {
	@Test
	@DisplayName("Post 생성 시 기본 값 설정 확인")
	void softDelete_setsDeletedAt() {
		// given
		Post post = Post.builder().title("T").content("C").build();
		Clock fixedClock = Clock.fixed(
			Instant.parse("2025-06-20T10:15:30Z"), ZoneOffset.UTC);

		// when
		post.softDelete(fixedClock);

		// then
		assertEquals(post.getDeletedAt(),
			LocalDateTime.ofInstant(Instant.parse("2025-06-20T10:15:30Z"),
				ZoneOffset.UTC));
	}

	@Test
	@DisplayName("Post 수정 시 updatedAt 값이 변경되는지 확인")
	void post_update_changesUpdatedAt() {
		// given
		Post post = Post.builder().title("T").content("C").build();
		PostCreateRequest editRequest = PostCreateRequest.builder()
			.title("Changed Title")
			.content("Changed Content")
			.build();

		Clock fixedClock = Clock.fixed(
			Instant.parse("2025-06-20T10:15:30Z"), ZoneOffset.UTC);

		// when
		post.update(editRequest, fixedClock);

		// then
		//assertNotEquals(beforeUpdate, post.getUpdatedAt());
		assertEquals("Changed Title", post.getTitle());
		assertEquals("Changed Content", post.getContent());
		assertEquals(LocalDateTime.ofInstant(
			Instant.parse("2025-06-20T10:15:30Z"), ZoneOffset.UTC),
			post.getUpdatedAt());
	}
}