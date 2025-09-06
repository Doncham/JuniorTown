package org.juniortown.backend.post.service;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReadCountRedisService {
	@Qualifier("keyCheckRedisTemplate")
	private final RedisTemplate<String, Boolean> keyCheckRedisTemplate;
	@Qualifier("readCountRedisTemplate")
	private final RedisTemplate<String, Long> readCountRedisTemplate;
	// 조회수증분키 형태, post:viewCount:{postId}:{userId}
	public static final String VIEW_COUNT_KEY = "post:viewCount:";
	// 중복방지키 형태, postDup:key:{postId}:{userId}
	public static final String DUP_PREVENT_KEY = "postDup:key:";

	public Long readCountUp(String userId, String postId) {
		String dupKey = buildDupPreventKey(postId, userId);
		try {
			// dupKey 조회
			Boolean exist = keyCheckRedisTemplate.opsForValue().get(dupKey);
			if (exist != null && exist) {
				// 10분이 지나지 않았으니 조회수가 늘지 않는다.
				log.info("이미 최근에 조회한 게시글 - 게시글 카운트 증가 x");
				return getReadCount(postId);
			} else {
				// 중복키 생성
				keyCheckRedisTemplate.opsForValue().set(dupKey, true, 10, TimeUnit.MINUTES);
				// 조회수 캐시++1
				//readCountRedisTemplate.opsForValue().increment(readCountKey);
				readCountRedisTemplate.opsForHash().increment(VIEW_COUNT_KEY, postId,1);
				return getReadCount(postId);
			}
		} catch (Exception e) {
			log.error("Redis 조회수 처리 중 오류 발생: postId={}, userId={}", postId, userId, e);
			// 예외 발생 시에도 조회수는 0으로 반환
			return 0L;
		}
	}

	public Long getReadCount(String postId) {
		//Long readCount = readCountRedisTemplate.opsForValue().get(readCountKey);
		Long readCount = (Long)readCountRedisTemplate.opsForHash().get(VIEW_COUNT_KEY, postId);
		return readCount != null ? readCount : 0L;
	}

	public Map<String, Long> getReadDeltaFromRedis(List<Long> postIds) {
		if(postIds == null || postIds.isEmpty()) return Map.of();

		Collection<Object> postIdKeys = postIds.stream()
			.map(String::valueOf)
			.collect(Collectors.toList());

		// multiGet으로 캐시된 조회수 가져오기
		List<Object> deltas = readCountRedisTemplate.opsForHash()
			.multiGet(VIEW_COUNT_KEY, postIdKeys);

		HashMap<String, Long> map = new HashMap<>();
		for (int i = 0; i < postIds.size(); i++) {
			// 여기서도 바로 (Long)으로 캐스팅 하면 위험한가?
			Object readCountCache = deltas.get(i);
			if(readCountCache == null) readCountCache = 0L;
			map.put(String.valueOf(postIds.get(i)), (Long)readCountCache);
		}
		return map;
	}

	private String buildDupPreventKey(String postId, String userId) {
		return DUP_PREVENT_KEY + postId + ":" + userId;
	}
}
