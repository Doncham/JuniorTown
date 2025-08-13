package org.juniortown.backend.post.service;

import java.time.Clock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.juniortown.backend.comment.dto.response.CommentsInPost;
import org.juniortown.backend.comment.entity.Comment;
import org.juniortown.backend.comment.repository.CommentRepository;
import org.juniortown.backend.comment.service.CommentTreeBuilder;
import org.juniortown.backend.like.entity.Like;
import org.juniortown.backend.like.repository.LikeRepository;
import org.juniortown.backend.post.dto.request.PostCreateRequest;
import org.juniortown.backend.post.dto.response.PostDetailResponse;
import org.juniortown.backend.post.dto.response.PostResponse;
import org.juniortown.backend.post.dto.response.PostWithLikeCountProjection;
import org.juniortown.backend.post.entity.Post;
import org.juniortown.backend.post.exception.PostDeletePermissionDeniedException;
import org.juniortown.backend.post.exception.PostNotFoundException;
import org.juniortown.backend.post.exception.PostUpdatePermissionDeniedException;
import org.juniortown.backend.post.repository.PostRepository;
import org.juniortown.backend.user.entity.User;
import org.juniortown.backend.user.exception.UserNotFoundException;
import org.juniortown.backend.user.repository.UserRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {
	private final PostRepository postRepository;
	private final UserRepository userRepository;
	private final LikeRepository likeRepository;
	private final ViewCountService viewCountService;
	private final CommentRepository commentRepository;
	private final Clock clock;
	private final RedisTemplate<String, Long> redisTemplate;
	private final static int PAGE_SIZE = 10;

	@Transactional
	public PostResponse create(Long userId, PostCreateRequest postCreateRequest) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new UserNotFoundException());

		Post savedPost = postRepository.save(postCreateRequest.toEntity(user));

		return PostResponse.from(savedPost);
	}

	@Transactional
	public void delete(Long postId, Long userId) {
		Post post = postRepository.findById(postId)
			.orElseThrow(() -> new PostNotFoundException());

		if (!post.getUser().getId().equals(userId)) {
			throw new PostDeletePermissionDeniedException();
		}

		//postRepository.delete(post);
		// soft delete
		post.softDelete(clock);
		log.info("게시글 삭제 성공: {}", post);

	}

	@Transactional
	public PostResponse update(Long postId, Long userId, PostCreateRequest postCreateRequest) {
		Post post = postRepository.findById(postId)
			.orElseThrow(() -> new PostNotFoundException());

		if (!post.getUser().getId().equals(userId)) {
			throw new PostUpdatePermissionDeniedException();
		}

		post.update(postCreateRequest, clock);
		log.info("게시글 업데이트 성공: {}", post);
		return PostResponse.from(post);
	}
	@Transactional(readOnly = true)
	public Page<PostResponse> getPosts(Long userId, int page) {
		Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")));

		Page<PostWithLikeCountProjection> postPage = (userId == null)
			? postRepository.findAllWithLikeCountForNonUser(pageable)
			: postRepository.findAllWithLikeCountForUser(userId, pageable);

		Map<Long,Long> redisReadCounts = getReadCountFromRedisCache(postPage);

		List<PostResponse> content = postPage.getContent().stream()
			.map(post -> PostResponse.builder()
			   .id(post.getId())
			   .title(post.getTitle())
			   .userId(post.getUserId())
			   .userName(post.getUsername())
			   .likeCount(post.getLikeCount())
			   .isLiked(Boolean.TRUE.equals(post.getIsLiked()))
			   .createdAt(post.getCreatedAt())
			   .updatedAt(post.getUpdatedAt())
			   .readCount(post.getReadCount() + redisReadCounts.getOrDefault(post.getId(), 0L))
			   .build())
			.collect(Collectors.toList());


		return new PageImpl<>(content, pageable, postPage.getTotalElements());
	}

	private Map<Long,Long> getReadCountFromRedisCache(Page<PostWithLikeCountProjection> postPage) {
		List<Long> ids = postPage.getContent().stream()
			.map(PostWithLikeCountProjection::getId)
			.toList();
		if(ids.isEmpty()) return Map.of();

		List<String> keys = ids.stream()
			.map(id -> ViewCountService.VIEW_COUNT_KEY + id)
			.toList();
		try {
			List<Long> readCounts = redisTemplate.opsForValue().multiGet(keys);

			HashMap<Long, Long> map = new HashMap<>();
			for (int i = 0; i < ids.size(); i++) {
				Long readCount = (readCounts != null && i < readCounts.size()) ? readCounts.get(i) : null;
				map.put(ids.get(i), (readCount != null) ? readCount : 0L);
			}
			return map;
		} catch (DataAccessException e) {
			log.error("Redis 장애: {}", e.getMessage());
			return ids.stream().collect(Collectors.toMap(id -> id, id -> 0L));
		}
	}

	@Transactional(readOnly = true)
	public PostDetailResponse getPost(Long postId, String viewerId) {
		Post post = postRepository.findById(postId)
			.orElseThrow(() -> new PostNotFoundException());
		// 삭제된 게시글은 조회할 수 없음, 하드 코딩을 통한 삭제된 게시글을 조회 시도 차단
		if (post.getDeletedAt() != null) {
			throw new PostNotFoundException();
		}
		boolean isMember = isLong(viewerId);
		Optional<Like> like = Optional.empty();
		if(isMember) {
			like = likeRepository.findByUserIdAndPostId(Long.valueOf(viewerId), post.getId());
		}
		Long likeCount = likeRepository.countByPostId(postId);
		Long redisReadCount = viewCountService.readCountUp(viewerId, postId.toString());

		// 댓글 조회 로직
		List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtAsc(postId);
		List<CommentsInPost> commentTree = CommentTreeBuilder.build(comments);

		return PostDetailResponse.builder()
			.id(post.getId())
			.title(post.getTitle())
			.content(post.getContent())
			.userId(post.getUser().getId())
			.userName(post.getUser().getName())
			.likeCount(likeCount)
			.isLiked(like.isPresent())
			.readCount(post.getReadCount() + redisReadCount)
			.comments(commentTree)
			.createdAt(post.getCreatedAt())
			.updatedAt(post.getUpdatedAt())
			.deletedAt(post.getDeletedAt())
			.build();
	}

	private boolean isLong(String str) {
		try {
			Long.parseLong(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
}
