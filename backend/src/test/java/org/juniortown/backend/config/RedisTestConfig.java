package org.juniortown.backend.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@TestConfiguration
@Profile("test")
public class RedisTestConfig {
	@Value("${spring.data.redis.host}")
	private String host;
	@Value("${spring.data.redis.port}")
	private int port;
	@Bean
	public RedisConnectionFactory redisConnectionFactory() {
		return new LettuceConnectionFactory(host,port); // host/port는 properties에서 spring이 읽어감
	}
	@Bean
	public RedisTemplate<String, Boolean> keyCheckRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
		RedisTemplate<String,Boolean> redisTemplate = new RedisTemplate<>();
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(Boolean.class));
		redisTemplate.setConnectionFactory(redisConnectionFactory);
		return redisTemplate;
	}

	@Bean
	public RedisTemplate<String, Long> readCountRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
		RedisTemplate<String,Long> redisTemplate = new RedisTemplate<>();
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(Long.class));
		redisTemplate.setConnectionFactory(redisConnectionFactory);
		return redisTemplate;
	}
}

