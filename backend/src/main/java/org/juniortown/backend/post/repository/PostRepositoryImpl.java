package org.juniortown.backend.post.repository;

import java.util.List;

import org.juniortown.backend.post.entity.Post;

import org.juniortown.backend.post.dto.PostSearch;
import org.juniortown.backend.post.entity.QPost;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom{
	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public List<Post> getList(PostSearch postSearch) {
		return jpaQueryFactory.selectFrom(QPost.post)
			.limit(postSearch.getSize())
			.offset(postSearch.getOffset())
			.orderBy(QPost.post.id.desc())
			.fetch();
	}
}
