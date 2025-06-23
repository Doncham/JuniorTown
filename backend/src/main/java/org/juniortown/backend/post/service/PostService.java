package org.juniortown.backend.post.service;

import java.time.Clock;

import org.juniortown.backend.post.exception.PostNotFoundException;
import org.juniortown.backend.post.dto.request.PostCreateRequest;
import org.juniortown.backend.post.dto.response.PostResponse;
import org.juniortown.backend.post.entity.Post;
import org.juniortown.backend.post.exception.PostDeletePermissionDeniedException;
import org.juniortown.backend.post.exception.PostUpdatePermissionDeniedException;
import org.juniortown.backend.post.repository.PostRepository;
import org.juniortown.backend.user.entity.User;
import org.juniortown.backend.user.exception.UserNotFoundException;
import org.juniortown.backend.user.repository.UserRepository;
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
	private final Clock clock;
	@Transactional
	public PostResponse create(Long userId, PostCreateRequest postCreateRequest) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new UserNotFoundException());

		Post savedPost = postRepository.save(postCreateRequest.toEntity(user));

		return new PostResponse(savedPost);
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
		return new PostResponse(post);
	}
}
