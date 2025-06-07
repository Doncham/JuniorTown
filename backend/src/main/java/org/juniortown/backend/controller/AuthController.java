package org.juniortown.backend.controller;

import java.util.Base64;
import java.util.Date;

import javax.crypto.SecretKey;

import org.juniortown.backend.config.AppConfig;
import org.juniortown.backend.request.Login;
import org.juniortown.backend.request.Signup;
import org.juniortown.backend.response.SessionResponse;
import org.juniortown.backend.service.AuthService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController {
	private final AuthService authService;
	private final AppConfig appConfig;
	@PostMapping("/auth/login")
	public SessionResponse login(@RequestBody Login login) {
		Long userId = authService.signIn(login);

		// SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
		// byte[] encodedKey = key.getEncoded();
		// String strKey = Base64.getEncoder().encodeToString(encodedKey);

		SecretKey key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(appConfig.getJwtKey()));
		String jws = Jwts.builder()
			.setSubject(String.valueOf(userId))
			.signWith(key)
			.setIssuedAt(new Date())
			.compact();

		return new SessionResponse(jws);
	}

	@PostMapping("/auth/signup")
	public void signup(@RequestBody Signup signup) {
		authService.signUp(signup);
	}
}
