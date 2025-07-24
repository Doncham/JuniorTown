package org.juniortown.backend.post.service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.juniortown.backend.post.entity.Post;
import org.juniortown.backend.post.repository.PostRepository;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
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
	private final RedisTemplate<String, Long> redisTemplate;
	private final PostRepository postRepository;
	private final RedissonClient redissonClient;
	public static final String SYNC_LOCK_KEY = "sync:viewCount:lock";

	@Scheduled(fixedDelay = 5 * 60 * 1000) // 5분마다 실행
	public void syncViewCounts() {
		RLock lock = redissonClient.getLock(SYNC_LOCK_KEY);
		boolean available = false;
		try {
			available = lock.tryLock(10, 300, TimeUnit.SECONDS);
			if(!available){
				log.info("동기화 락을 획득하지 못했습니다. 다른 인스턴스에세 이미 실행 중입니다.");
				return;
			}
			Set<String> keys = redisTemplate.keys("post:viewCount:*");
			if(keys == null) return;
			for (String key : keys) {
				Long postId = Long.valueOf(key.split(":")[2]);
				Long count = redisTemplate.opsForValue().get(key);
				if(count == null || count == 0) continue;

				postRepository.findById(postId).ifPresent(post -> {
					post.addReadCount(post.getReadCount() + count);
				});
				redisTemplate.delete(key);
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		} finally {
			if(available) lock.unlock();
		}
	}
}
