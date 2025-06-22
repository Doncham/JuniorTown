package org.juniortown.backend.post.entity;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.Time;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

class PostTest {
	@Test
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
}