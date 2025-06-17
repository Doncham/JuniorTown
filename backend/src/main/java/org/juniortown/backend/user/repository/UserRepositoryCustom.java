package org.juniortown.backend.user.repository;

import java.util.List;

import org.juniortown.backend.user.entity.User;

public interface UserRepositoryCustom {
	/****
 * Retrieves a list of all User entities.
 *
 * @return a list containing User objects
 */
List<User> getUsers();
}
