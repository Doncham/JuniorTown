package org.juniortown.backend.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest
class PostControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@Test
	@DisplayName("/post 요청 시 Hello World를 출력한다.")
	void test() throws Exception {
		// expected
		mockMvc.perform(MockMvcRequestBuilders.post("/posts")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\":\"제목입니다.\", \"content\": \"내용입니다.\"}")
			)
			.andExpect(status().isOk())
			.andExpect(content().string("{}"))
			.andDo(print());
	}

	@Test
	@DisplayName("/post 요청 시 title 값은 필수다.")
	void test2() throws Exception {
		// expected
		mockMvc.perform(MockMvcRequestBuilders.post("/posts")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\": \"\", \"content\": \"내용입니다.\"}")
			)
			.andExpect(jsonPath("$.code").value("400"))
			.andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
			.andExpect(jsonPath("$.validation.title").value("타이틀을 입력해주세요."))
			.andExpect(status().isBadRequest())
			.andDo(print());
	}

}