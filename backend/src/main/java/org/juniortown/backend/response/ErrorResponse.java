package org.juniortown.backend.response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * {
 *     "code": "400",
 *     "message": "잘못된 요청입니다.",
 *     "validation": {
 *         "title": ["값을 입력해주세요"]
 *     }
 * }
 */
@Getter
//@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
public class ErrorResponse {
	private final String code;
	private final String message;
	private final Map<String, List<String>> validation = new HashMap<>();
	@Builder
	public ErrorResponse(String code, String message) {
		this.code = code;
		this.message = message;
	}

	public void addValidation(String fieldName, String errorMessage) {
		this.validation.computeIfAbsent(fieldName, key -> new ArrayList<>())
			.add(errorMessage);
	}
}
