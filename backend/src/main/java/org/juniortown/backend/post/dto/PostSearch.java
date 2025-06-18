package org.juniortown.backend.post.dto;

import static java.lang.Math.*;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PostSearch {
	private static final int MAX_SIZE = 2000;
	@Builder.Default
	private int page = 1;
	@Builder.Default
	private int size = 10;

	public long getOffset() {
		return (long)(max(page, 1) - 1 ) * min(size, MAX_SIZE);
	}
}
