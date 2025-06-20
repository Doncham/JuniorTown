package org.juniortown.backend.post.controller;

import org.juniortown.backend.post.dto.request.PostCreateRequest;
import org.juniortown.backend.post.dto.response.PostResponse;
import org.juniortown.backend.post.service.PostService;
import org.juniortown.backend.user.dto.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PostController {
	private final PostService postService;
	@PostMapping("/posts")
	public ResponseEntity<PostResponse> create(@AuthenticationPrincipal CustomUserDetails customUserDetails,
		@Valid @RequestBody PostCreateRequest postCreateRequest) {
		// 이거 null 체크를 해야하나?
		Long userId = customUserDetails.getUserId();
		PostResponse postResponse = postService.create(userId, postCreateRequest);
		return ResponseEntity.status(HttpStatus.CREATED).body(postResponse);
	}

	@DeleteMapping("/posts")
	public ResponseEntity<?> delete() {
		//PostService .delete();
		return ResponseEntity.ok().build();
	}

	@PatchMapping("/posts")
	public ResponseEntity<?> update() {
		// PostService.update();
		return ResponseEntity.ok().build();
	}


}
