package org.juniortown.backend.controller;

import org.juniortown.backend.config.AppConfig;
import org.juniortown.backend.request.Signup;
import org.juniortown.backend.service.AuthService;

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
	private final AppConfig appConfig;
	@PostMapping("/auth/signup")
	public void signup(@RequestBody Signup signup) {
		authService.signUp(signup);
	}
}
