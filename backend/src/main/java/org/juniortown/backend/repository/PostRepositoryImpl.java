package org.juniortown.backend.repository;

import java.util.List;

import org.juniortown.backend.domain.Post;
import org.juniortown.backend.domain.QPost;
import org.juniortown.backend.request.PostSearch;

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
