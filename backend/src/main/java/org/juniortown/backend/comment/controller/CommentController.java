package org.juniortown.backend.comment.controller;

import org.juniortown.backend.comment.dto.request.CommentCreateRequest;
import org.juniortown.backend.comment.dto.response.CommentCreateResponse;
import org.juniortown.backend.comment.service.CommentService;
import org.juniortown.backend.user.dto.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CommentController {
	private final CommentService commentService;
	@PostMapping("/comment")
	public ResponseEntity<CommentCreateResponse> createComment(@AuthenticationPrincipal CustomUserDetails customUserDetails,
		@Valid @RequestBody CommentCreateRequest commentCreateRequest) {
		Long userId = customUserDetails.getUserId();

		CommentCreateResponse response = commentService.createComment(userId, commentCreateRequest);
		return ResponseEntity.ok(response);
	}
}
