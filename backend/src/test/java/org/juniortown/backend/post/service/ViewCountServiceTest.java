package org.juniortown.backend.post.service;

import static org.mockito.Mockito.*;

import java.util.concurrent.TimeUnit;

import org.juniortown.backend.config.RedisTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ViewCountServiceTest {
	@Mock
	private ViewCountService viewCountService;
	@Mock
	private RedisTemplate<String, Boolean> keyCheckRedisTemplate;
	@Mock
	private ValueOperations<String, Boolean> keyCheckValueOperations;
	@Mock
	private RedisTemplate<String, Long> readCountRedisTemplate;
	@Mock
	private ValueOperations<String, Long> readCountValueOperations;

	@BeforeEach
	public void setUp() {
		viewCountService = new ViewCountService(keyCheckRedisTemplate, readCountRedisTemplate);
	}

	@Test
	@DisplayName("조회수 증가 성공 - 중복키 존재 x")
	void incrementViewCount_success() {
		// given
		String userId = "1";
		String postId = "1";
		String dupKey = "postDup:key:" + postId + ":" + userId;
		String readCountKey = "post:viewCount:" + postId;

		when(keyCheckRedisTemplate.opsForValue()).thenReturn(keyCheckValueOperations);
		when(readCountRedisTemplate.opsForValue()).thenReturn(readCountValueOperations);
		when(keyCheckValueOperations.get(dupKey)).thenReturn(null);
		// when
		viewCountService.readCountUp(userId, postId);

		// then
		verify(keyCheckValueOperations).set(eq(dupKey), eq(true), eq(10L), eq(TimeUnit.MINUTES));
		verify(readCountRedisTemplate.opsForValue()).increment(readCountKey);
	}

	@Test
	@DisplayName("조회수 증가 실패 - 중복키 존재 o")
	void incrementViewCount_fail() {
		// given
		String userId = "1";
		String postId = "1";
		String dupKey = "postDup:key:" + postId + ":" + userId;
		String readCountKey = "post:viewCount:" + postId;

		when(keyCheckRedisTemplate.opsForValue()).thenReturn(keyCheckValueOperations);
		when(readCountRedisTemplate.opsForValue()).thenReturn(readCountValueOperations);
		when(keyCheckValueOperations.get(dupKey)).thenReturn(true);
		when(readCountValueOperations.get(readCountKey)).thenReturn(1L);
		// when
		viewCountService.readCountUp(userId, postId);

		// then
		verify(keyCheckValueOperations, never()).set(eq(dupKey), eq(true), eq(10L), eq(TimeUnit.MINUTES));
		verify(readCountRedisTemplate.opsForValue()).get(readCountKey);
	}
}