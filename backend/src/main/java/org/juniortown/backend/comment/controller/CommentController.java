package org.juniortown.backend.comment.controller;

import org.juniortown.backend.comment.dto.request.CommentCreateRequest;
import org.juniortown.backend.comment.dto.request.CommentUpdateRequest;
import org.juniortown.backend.comment.dto.response.CommentCreateResponse;
import org.juniortown.backend.comment.service.CommentService;
import org.juniortown.backend.user.dto.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
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
public class CommentController {
	private final CommentService commentService;
	@PostMapping("/comments")
	public ResponseEntity<CommentCreateResponse> createComment(@AuthenticationPrincipal CustomUserDetails customUserDetails,
		@Valid @RequestBody CommentCreateRequest commentCreateRequest) {
		Long userId = customUserDetails.getUserId();

		CommentCreateResponse response = commentService.createComment(userId, commentCreateRequest);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@DeleteMapping("/comments/{commentId}")
	public ResponseEntity<?> CommentDeleteRequest(@AuthenticationPrincipal CustomUserDetails customUserDetails,
		@PathVariable Long commentId) {
		Long userId = customUserDetails.getUserId();
		commentService.deleteComment(userId, commentId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
	@PatchMapping("/comments/{commentId}")
	public ResponseEntity<?> CommentUpdateRequest(@AuthenticationPrincipal CustomUserDetails customUserDetails,
		@PathVariable Long commentId, @Valid @RequestBody CommentUpdateRequest commentUpdateRequest) {
		Long userId = customUserDetails.getUserId();
		commentService.updateComment(userId, commentId, commentUpdateRequest);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
}
