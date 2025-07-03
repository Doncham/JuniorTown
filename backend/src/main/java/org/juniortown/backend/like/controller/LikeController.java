package org.juniortown.backend.like.controller;

import org.juniortown.backend.like.dto.response.LikeResponse;
import org.juniortown.backend.like.service.LikeService;
import org.juniortown.backend.user.dto.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LikeController {
	private final LikeService likeService;
	@PostMapping("/posts/likes/{postId}")
	public ResponseEntity<LikeResponse> like(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long postId) {
		Long userId = customUserDetails.getUserId();
		LikeResponse response = likeService.likePost(userId, postId);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
}
