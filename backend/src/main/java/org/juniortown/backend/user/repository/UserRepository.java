package org.juniortown.backend.user.repository;

import java.util.Optional;

import org.juniortown.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {
	/****
 * Retrieves a user matching the specified email and password.
 *
 * @param email the user's email address
 * @param password the user's password
 * @return an Optional containing the user if found, or empty if no match exists
 */
Optional<User> findByEmailAndPassword(String email, String password);

	/****
 * Retrieves a user by their email address.
 *
 * @param email the email address to search for
 * @return an Optional containing the user if found, or empty if no user exists with the specified email
 */
Optional<User> findByEmail(String email);

	/****
 * Checks if a user exists with the specified email address.
 *
 * @param email the email address to check for existence
 * @return true if a user with the given email exists, false otherwise
 */
Boolean existsByEmail(String email);
}
