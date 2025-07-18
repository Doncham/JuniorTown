package org.juniortown.backend.post.controller;

import org.juniortown.backend.post.dto.request.PostCreateRequest;
import org.juniortown.backend.post.dto.response.PageResponse;
import org.juniortown.backend.post.dto.response.PostWithLikeCount;
import org.juniortown.backend.post.dto.response.PostResponse;
import org.juniortown.backend.post.dto.response.PostWithLikeCountProjection;
import org.juniortown.backend.post.service.PostService;
import org.juniortown.backend.user.dto.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

	@DeleteMapping("/posts/{postId}")
	public ResponseEntity<?> delete(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long postId) {
		Long userId = customUserDetails.getUserId();
		postService.delete(postId, userId);
		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/posts/{postId}")
	public ResponseEntity<PostResponse> update(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long postId, @Valid @RequestBody PostCreateRequest postCreateRequest) {
		Long userId = customUserDetails.getUserId();
		PostResponse response = postService.update(postId, userId, postCreateRequest);
		// 202가 뭐임?
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	// 게시글 목록 조회, 페이지네이션 적용
	@GetMapping("/posts/{page}")
	public ResponseEntity<PageResponse<PostWithLikeCountProjection>> getPosts(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable int page) {
		Long userId = customUserDetails.getUserId();
		// null체크

	 	Page<PostWithLikeCountProjection> posts = postService.getPosts(userId, page);
		PageResponse<PostWithLikeCountProjection> response = new PageResponse<>(posts);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/posts/details/{postId}")
	public ResponseEntity<PostResponse> getPost(@PathVariable Long postId) {
		PostResponse postResponse = postService.getPost(postId);
		return ResponseEntity.ok(postResponse);
	}

}
