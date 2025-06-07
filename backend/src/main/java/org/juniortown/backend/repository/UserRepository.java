package org.juniortown.backend.repository;

import java.util.Optional;

import org.juniortown.backend.domain.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {
	Optional<User> findByEmailAndPassword(String email, String password);

	Optional<User> findByEmail(String email);
}
