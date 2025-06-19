package org.juniortown.backend.post.service;

import org.juniortown.backend.post.dto.request.PostCreateRequest;
import org.juniortown.backend.post.dto.response.PostResponse;
import org.juniortown.backend.post.entity.Post;
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
	@Transactional
	public PostResponse create(Long userId, PostCreateRequest postCreateRequest) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new UserNotFoundException());

		Post savedPost = postRepository.save(postCreateRequest.toEntity(user));

		return new PostResponse(savedPost);
	}
}
