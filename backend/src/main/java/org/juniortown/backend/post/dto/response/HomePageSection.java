package org.juniortown.backend.post.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class HomePageSection {
	private String category;
	private List<PostForHomePage> items;
	@Builder
	public HomePageSection(String category, List<PostForHomePage> items) {
		this.category = category;
		this.items = items;
	}
}
