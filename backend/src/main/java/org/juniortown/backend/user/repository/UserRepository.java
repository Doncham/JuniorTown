package org.juniortown.backend.user.repository;

import java.util.Optional;

import org.juniortown.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {

	Optional<User> findByEmail(String email);

	Boolean existsByEmail(String email);
}
