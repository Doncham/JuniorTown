package org.juniortown.backend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.juniortown.backend.request.PostCreate;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController()
public class PostController {
	@PostMapping("/posts")
	public Map<String,String> post(@RequestBody @Valid PostCreate param) {
		/*if(result.hasErrors()){
			List<FieldError> fieldErrors = result.getFieldErrors();
			FieldError firstFieldError = fieldErrors.get(0);
			String fieldName = firstFieldError.getField();
			String errorMessage = firstFieldError.getDefaultMessage();

			Map<String, String> error = new HashMap<>();
			error.put(fieldName, errorMessage);
			return error;
		}*/

		return Map.of();
	}

}
