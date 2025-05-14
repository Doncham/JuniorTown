package org.juniortown.backend.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.juniortown.backend.domain.Post;
import org.juniortown.backend.repository.PostRepository;
import org.juniortown.backend.request.PostCreate;
import org.juniortown.backend.response.PostResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

	public PostResponse get(Long id) {
		Post post = postRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 글입니다."));

		PostResponse response = PostResponse.builder()
			.id(post.getId())
			.title(post.getTitle())
			.content(post.getContent())
			.build();
		return response;
	}

	public List<PostResponse> getList(Pageable pageable) {
		return postRepository.findAll(pageable).stream()
			.map(post -> new PostResponse(post))
			.collect(Collectors.toList());
	}
}
