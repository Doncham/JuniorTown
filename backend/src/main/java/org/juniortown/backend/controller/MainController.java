package org.juniortown.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {
	@GetMapping("/")
	public String main() {
		return "메인 페이지입니다.";
	}

	@GetMapping("/botWorking")
	public String bot() {
		return "bot are working";
	}
}
