package org.juniortown.backend.post.entity;

import lombok.Getter;

@Getter
public enum Category {
	QUESTION(),
	STUDY(),
	NOTICE(),
	SECOND_HAND_MARKET(),
	MENTOR();

	public static Category from(String value) {
		if (value == null) {
			throw new IllegalArgumentException("Category는 null일 수 없다.");
		}
		try {
			return Category.valueOf(value.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("유효하지 않은 카테고리입니다: " + value);
		}
	}
}
