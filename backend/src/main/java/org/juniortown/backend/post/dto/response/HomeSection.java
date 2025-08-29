package org.juniortown.backend.post.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class HomeSection {
	private String category;
	private List<PostForHome> items;
	@Builder
	public HomeSection(String category, List<PostForHome> items) {
		this.category = category;
		this.items = items;
	}
}
