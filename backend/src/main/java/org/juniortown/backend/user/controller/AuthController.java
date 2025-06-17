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
	/**
	 * Handles user registration requests.
	 *
	 * Accepts a JSON payload containing user sign-up information, processes the registration, and responds with HTTP 201 Created upon success.
	 *
	 * @param signUpDTO the user registration data
	 * @return HTTP 201 Created with no response body if registration is successful
	 */
	@PostMapping("/auth/signup")
	public ResponseEntity<Void> signup(@Valid @RequestBody SignUpDTO signUpDTO) {
		authService.signUp(signUpDTO);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	/**
	 * Handles a test POST request to verify JWT authentication.
	 *
	 * This endpoint can be used to confirm that JWT-based authentication is functioning correctly.
	 */
	@PostMapping("/auth/jwt/test")
	public void test() {
		log.info("test");
	}
}
