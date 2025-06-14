package org.juniortown.backend.user.repository;

import java.util.List;

import org.juniortown.backend.user.entity.User;

public interface UserRepositoryCustom {
	List<User> getUsers();
}
