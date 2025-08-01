package org.juniortown.backend.config;

import java.time.Clock;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
@TestConfiguration
public class TestClockNotMockConfig {
	@Bean
	public Clock clock() {
		return Clock.systemUTC();
	}
}
