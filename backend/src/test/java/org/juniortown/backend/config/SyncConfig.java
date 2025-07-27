package org.juniortown.backend.config;

import org.mockito.Mockito;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@TestConfiguration
@Profile("test")
public class SyncConfig {
	@Bean
	public RedissonClient redissonClient() {
		// 테스트용 Mock 객체 반환 (실제 Redis 연결 X)
		return Mockito.mock(RedissonClient.class);
	}
}
