package org.juniortown.backend.post.service;

import static org.mockito.Mockito.*;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class ViewCountServiceTest {
	private ReadCountRedisService readCountRedisService;
	@Mock
	private RedisTemplate<String, Boolean> keyCheckRedisTemplate;
	@Mock
	private ValueOperations<String, Boolean> keyCheckValueOperations;
	@Mock
	private RedisTemplate<String, Long> readCountRedisTemplate;
	@Mock
	private HashOperations<String, String, Long> readCountValueOperations;

	@BeforeEach
	public void setUp() {
		readCountRedisService = new ReadCountRedisService(keyCheckRedisTemplate, readCountRedisTemplate);
	}

	@Test
	@DisplayName("조회수 증가 성공 - 중복키 존재 x")
	void incrementViewCount_success() {
		// given
		String userId = "1";
		String postId = "1";
		String dupKey = "postDup:key:" + postId + ":" + userId;
		String readCountKey = ReadCountRedisService.VIEW_COUNT_KEY;

		when(keyCheckRedisTemplate.opsForValue()).thenReturn(keyCheckValueOperations);
		when(readCountRedisTemplate.<String, Long>opsForHash()).thenReturn(readCountValueOperations);
		when(keyCheckValueOperations.get(dupKey)).thenReturn(null);
		when(readCountValueOperations.get(readCountKey, postId)).thenReturn(1L);
		// when
		Long result = readCountRedisService.readCountUp(userId, postId);

		// then
		Assertions.assertEquals(1L, result);
		verify(keyCheckValueOperations).set(eq(dupKey), eq(true), eq(10L), eq(TimeUnit.MINUTES));
		verify(readCountRedisTemplate.opsForHash()).increment(readCountKey, postId, 1);
	}

	@Test
	@DisplayName("조회수 증가 실패 - 중복키 존재 o")
	void incrementViewCount_fail() {
		// given
		String userId = "1";
		String postId = "1";
		String dupKey = "postDup:key:" + postId + ":" + userId;
		String readCountKey = ReadCountRedisService.VIEW_COUNT_KEY;

		when(keyCheckRedisTemplate.opsForValue()).thenReturn(keyCheckValueOperations);
		when(readCountRedisTemplate.<String,Long>opsForHash()).thenReturn(readCountValueOperations);
		when(keyCheckValueOperations.get(dupKey)).thenReturn(true);
		when(readCountValueOperations.get(readCountKey, postId)).thenReturn(0L);
		// when
		Long result = readCountRedisService.readCountUp(userId, postId);

		// then
		Assertions.assertEquals(0L, result);
		verify(keyCheckValueOperations, never()).set(eq(dupKey), eq(true), eq(10L), eq(TimeUnit.MINUTES));
		verify(readCountValueOperations, never()).increment(readCountKey, postId,1);
		verify(readCountRedisTemplate.opsForHash()).get(readCountKey, postId);
	}
}