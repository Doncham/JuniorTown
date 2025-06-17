package org.juniortown.backend.user.repository;

import java.util.List;

import org.juniortown.backend.user.entity.User;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom{
	private final JPAQueryFactory jpaQueryFactory;
	/****
	 * Retrieves a list of users.
	 *
	 * @return a list of users, or null if not implemented
	 */
	@Override
	public List<User> getUsers() {
		return null;
	}
}
