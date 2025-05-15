package org.juniortown.backend.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.juniortown.backend.domain.Post;
import org.juniortown.backend.domain.PostEditor;
import org.juniortown.backend.repository.PostRepository;
import org.juniortown.backend.request.PostCreate;
import org.juniortown.backend.request.PostEdit;
import org.juniortown.backend.request.PostSearch;
import org.juniortown.backend.response.PostResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.transaction.TransactionScoped;
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

	public List<PostResponse> getList(PostSearch postSearch) {
		return postRepository.getList(postSearch).stream()
			.map(post -> new PostResponse(post))
			.collect(Collectors.toList());
	}
	@Transactional
	public void edit(Long id, PostEdit postEdit) {
		Post post = postRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 글입니다."));

		//post.change(postEdit.getTitle(), postEdit.getContent());

		// PostEditor.PostEditorBuilder editorBuilder = post.toEditor();
		//
		// PostEditor postEditor = editorBuilder.title(postEdit.getTitle())
		// 	.content(postEdit.getContent())
		// 	.build();

		post.edit(postEdit.getTitle(), postEdit.getContent());


	}

	public void delete(Long id) {
		Post post = postRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 글입니다"));

		postRepository.delete(post);

	}
}
