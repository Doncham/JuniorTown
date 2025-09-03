package org.juniortown.backend.post.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
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
		String readCountKey = buildReadCountKey(postId);
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
		String readCountKey = buildReadCountKey(postId);
		//Long readCount = readCountRedisTemplate.opsForValue().get(readCountKey);
		// 응? 뭘 반환해주는거지? String인가?
		Long readCount = (Long)readCountRedisTemplate.opsForHash().get(VIEW_COUNT_KEY, postId);
		return readCount != null ? readCount : 0L;
	}

	public Map<String, Long> getReadDeltaFromRedis(List<String> postIds) {
		if(postIds == null || postIds.isEmpty()) return Map.of();

		List<String> postIdKeys = postIds.stream()
			.map(i -> VIEW_COUNT_KEY + i)
			.collect(Collectors.toList());

		// multiGet으로 캐시된 조회수 가져오기
		List<Long> readCounts = readCountRedisTemplate.opsForValue().multiGet(postIdKeys);
		// Collections.singleton은 왜 쓰는거지????
		List<Object> raw = readCountRedisTemplate.opsForHash()
			.multiGet("view:delta", Collections.singleton(postIdKeys));

		HashMap<String, Long> map = new HashMap<>();
		for (int i = 0; i < postIds.size(); i++) {
			map.put(postIds.get(i), readCounts.get(i));
		}
		return map;
	}

	private String buildDupPreventKey(String postId, String userId) {
		return DUP_PREVENT_KEY + postId + ":" + userId;
	}

	private String buildReadCountKey(String postId) {
		return VIEW_COUNT_KEY + postId;
	}
}
