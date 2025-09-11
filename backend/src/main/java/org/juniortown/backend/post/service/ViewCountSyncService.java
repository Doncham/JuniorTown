package org.juniortown.backend.post.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.juniortown.backend.post.entity.Post;
import org.juniortown.backend.post.repository.PostRepository;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ViewCountSyncService {
	@Qualifier("readCountRedisTemplate")
	private final RedisTemplate<String, Long> redisTemplate;
	private final PostRepository postRepository;
	private final RedissonClient redissonClient;
	public static final String SYNC_LOCK_KEY = "sync:viewCount:lock";
	// 운영 해시 키
	private static final String SRC_KEY = ReadCountRedisService.VIEW_COUNT_KEY;
	// 한 번에 처리할 배치 크기
	private static final int BATCH = 1000;

	@Scheduled(fixedDelay = 5 * 60 * 1000) // 5분마다 실행
	public void syncViewCounts() {
		log.info("ViewCountSyncService.syncViewCounts() - 시작");
		RLock lock = redissonClient.getLock(SYNC_LOCK_KEY);
		boolean available = false;
		try {
			available = lock.tryLock(10, 300, TimeUnit.SECONDS);
			if (!available) {
				log.info("동기화 락을 획득하지 못했습니다. 다른 인스턴스에세 이미 실행 중입니다.");
				return;
			}
			String snapKey = SRC_KEY + ":processing:" + System.currentTimeMillis();
			boolean snapCreated = tryRenameIfExists(SRC_KEY, snapKey);
			if (!snapCreated) {
				log.info("스냅샷 생성 대상이 없습니다(키 없음). 종료합니다.");
				return;
			}
			ScanOptions scanOptions = ScanOptions.scanOptions().count(BATCH).build();
			try (Cursor<Map.Entry<Object, Object>> cursor = redisTemplate.opsForHash().scan(snapKey, scanOptions)) {
				List<Map.Entry<Object, Object>> chunk = new ArrayList<>(BATCH);

				while (cursor.hasNext()) {
					chunk.add(cursor.next());
					if (chunk.size() == BATCH) {
						applyChunkToDbOnly(chunk);
						chunk.clear();
					}
				}

					if (!chunk.isEmpty()) {
						applyChunkToDbOnly(chunk);
					}
				}
			redisTemplate.delete(snapKey);
			log.info("ViewCountSyncService.syncViewCounts() - 완료 (snapKey: {})", snapKey);

		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		} finally {
			if (available) lock.unlock();
		}
	}

	private void applyChunkToDbOnly(List<Map.Entry<Object, Object>> chunk) {
		for (Map.Entry<Object, Object> e : chunk) {
			Long postId = parsePostId(e.getKey());
			long delta = parseDelta(e.getValue());
			if(postId == null || delta == 0L) continue;

			postRepository.findById(postId).ifPresent((Post p) -> p.addReadCount(delta));
		}
	}
	private long parseDelta(Object val) {
		if (val == null) return 0L;
		// Number가 뭡니까?
		if (val instanceof Number) return ((Number) val).longValue();
		try {
			return Long.parseLong(String.valueOf(val));
		} catch (NumberFormatException ex) {
			log.warn("숫자 파싱 실패: {}", val);
			return 0L;
		}
	}

	private Long parsePostId(Object field) {
		if(field == null) return null;
		try {
			return Long.valueOf(String.valueOf(field));
		} catch (NumberFormatException e) {
			log.warn("잘못된 필드값(포스트 ID 아님): {}", field);
			return null;
		}
	}

	private boolean tryRenameIfExists(String srcKey, String snapKey) {
		try {
			redisTemplate.rename(srcKey, snapKey);
			return true;
		} catch (DataAccessException e) {
			log.debug("RENAME 실패 (아마 키 없음): srcKey={}, snapKey={}, cause={}", srcKey, snapKey, e.getMessage());
			return false;
		}
	}

	private Long getPostIdFromKey(String key) {
		String[] parts = key.split(":");
		if (parts.length < 3) {
			log.warn("잘못된 키 형식: {}", key);
			return null;
		}
		Long postId;
		try {
			postId = Long.valueOf(parts[2]);
		} catch (NumberFormatException e) {
			log.warn("키에서 postId를 추출할 수 없습니다: {}", key);
			return null;
		}
		return postId;
	}
}
