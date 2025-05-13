package org.juniortown.backend.service;

import java.util.Optional;

import org.juniortown.backend.domain.Post;
import org.juniortown.backend.repository.PostRepository;
import org.juniortown.backend.request.PostCreate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {
	private final PostRepository postRepository;
	public void write(PostCreate postCreate) {
		Post post = Post.builder()
			.title(postCreate.getTitle())
			.content(postCreate.getContent())
			.build();
		postRepository.save(post);
	}

	public Post get(Long id) {
		Post post = postRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 글입니다."));
		return post;
	}
}
