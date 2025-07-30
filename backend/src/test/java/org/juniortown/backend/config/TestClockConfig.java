package org.juniortown.backend.config;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
@Primary
public class TestClockConfig {
	@Bean
	public Clock clock() {
		return Clock.fixed(Instant.parse("2025-06-20T10:15:30Z"), ZoneOffset.UTC);
	}
}
