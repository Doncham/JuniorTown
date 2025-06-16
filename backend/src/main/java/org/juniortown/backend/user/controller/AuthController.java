package org.juniortown.backend.user.controller;

import org.juniortown.backend.user.request.SignUpDTO;
import org.juniortown.backend.user.service.AuthService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {
	private final AuthService authService;
	@PostMapping("/auth/signup")
	public ResponseEntity<Void> signup(@Valid @RequestBody SignUpDTO signUpDTO) {
		authService.signUp(signUpDTO);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@PostMapping("/auth/jwt/test")
	public void test() {
		log.info("test");
	}
}
