package org.juniortown.backend.post.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.Getter;

@Getter
public class PageResponse<T> {
	private List<T> content;
	private int page;
	private int size;
	private long totalElements;
	private int totalPages;
	private boolean hasNext;
	private boolean hasPrevious;
	public PageResponse(Page<T> pageData) {
		this.content       = pageData.getContent();
		this.page          = pageData.getNumber();
		this.size          = pageData.getSize();
		this.totalElements = pageData.getTotalElements();
		this.totalPages    = pageData.getTotalPages();
		this.hasNext       = pageData.hasNext();
		this.hasPrevious   = pageData.hasPrevious();
	}
}
