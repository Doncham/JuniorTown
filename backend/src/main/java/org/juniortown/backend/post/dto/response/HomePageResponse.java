package org.juniortown.backend.post.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class HomePageResponse {
	List<HomePageSection> sections;
	private LocalDateTime createdAt;
	@Builder
	public HomePageResponse(List<HomePageSection> sections, LocalDateTime createdAt) {
		this.sections = sections;
		this.createdAt = createdAt;
	}
}
