package org.juniortown.backend.user.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;
	private String email;
	private String password;
	private LocalDateTime createdAt;
	private String role;

	/**
	 * Constructs a new User with the specified name, email, password, and role, setting the creation time to the current date and time.
	 *
	 * @param name the user's name
	 * @param email the user's email address
	 * @param password the user's password
	 * @param role the user's role
	 */
	@Builder
	public User(String name, String email, String password, String role) {
		this.name = name;
		this.email = email;
		this.password = password;
		this.createdAt = LocalDateTime.now();
		this.role = role;
	}
}
