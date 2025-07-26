package org.juniortown.backend.post.service;

import java.util.concurrent.TimeUnit;

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
public class ViewCountService {
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
				readCountRedisTemplate.opsForValue().increment(readCountKey);
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
		Long readCount = readCountRedisTemplate.opsForValue().get(readCountKey);
		return readCount != null ? readCount : 0L;
	}

	private String buildDupPreventKey(String postId, String userId) {
		return DUP_PREVENT_KEY + postId + ":" + userId;
	}

	private String buildReadCountKey(String postId) {
		return VIEW_COUNT_KEY + postId;
	}
}
