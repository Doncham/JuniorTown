package org.juniortown.backend.config;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.Optional;

import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component("auditingDateTimeProvider")
@RequiredArgsConstructor
public class AuditingDateTimeProvider implements DateTimeProvider {
	private final Clock clock;

	@Override
	public Optional<TemporalAccessor> getNow() {
		return Optional.of(LocalDateTime.now(clock));
	}
}
