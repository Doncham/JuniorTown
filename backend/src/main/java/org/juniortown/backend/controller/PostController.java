package org.juniortown.backend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.juniortown.backend.domain.Post;
import org.juniortown.backend.request.PostCreate;
import org.juniortown.backend.service.PostService;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PostController {
	private final PostService postService;
	@PostMapping("/posts")
	public void post(@RequestBody @Valid PostCreate request) {
		// Case1. 저장한 데이터 Entity -> response로 응답하기
		// Case2. 저장한 데이터의 primary_id -> response로 응답하기
		postService.write(request);
	}

	// 단건 조회
	@GetMapping("/posts/{postId}")
	public Post get(@PathVariable(name = "postId") Long id) {
		Post post = postService.get(id);
		return post;
	}

}
