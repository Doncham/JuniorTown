package org.juniortown.backend.post.controller;

import org.juniortown.backend.post.dto.PostCreateDTO;
import org.juniortown.backend.post.service.PostService;
import org.springframework.http.ResponseEntity;
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
	PostService postService;
	@PostMapping("/post")
	public ResponseEntity<?> create(@Valid @RequestBody PostCreateDTO postCreateDTO) {
		PostService.create(postCreateDTO);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/post")
	public ResponseEntity<?> delete() {
		//PostService .delete();
		return ResponseEntity.ok().build();
	}

	@PatchMapping("/post")
	public ResponseEntity<?> update() {
		// PostService.update();
		return ResponseEntity.ok().build();
	}


}
