package org.juniortown.backend.controller;

import java.time.Duration;
import java.util.Optional;

import org.juniortown.backend.domain.User;
import org.juniortown.backend.exception.InvalidSignInformation;
import org.juniortown.backend.repository.UserRepository;
import org.juniortown.backend.request.Login;
import org.juniortown.backend.response.SessionResponse;
import org.juniortown.backend.service.AuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController {
	private final AuthService authService;
	@PostMapping("/auth/login")
	public ResponseEntity<Object> login(@RequestBody Login login) {
		// DB에서 조회
		String accessToken = authService.signIn(login);
		ResponseCookie cookie = ResponseCookie.from("SESSION", accessToken)
			.domain("localhost") // todo 서버 환경에 따른 분리 필요
			.path("/")
			.httpOnly(true)
			.secure(false)
			.maxAge(Duration.ofDays(30))
			.sameSite("Strict")
			.build();

		log.info(">>>>>>> cookie={}", cookie.toString());

		return ResponseEntity.ok()
			.header(HttpHeaders.SET_COOKIE, cookie.toString())
			.build();
	}
}
