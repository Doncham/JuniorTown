package org.juniortown.backend.user.dto;

import java.util.ArrayList;
import java.util.Collection;

import org.juniortown.backend.user.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {
	private final User user;
	/****
	 * Returns the authorities granted to the user.
	 *
	 * @return a collection containing a single authority representing the user's role
	 */
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Collection<GrantedAuthority> collection = new ArrayList<>();
		collection.add(new GrantedAuthority() {
			@Override
			public String getAuthority() {
				return user.getRole();
			}
		});

		return collection;
	}

	/**
		 * Returns the password of the user.
		 *
		 * @return the user's password
		 */
	@Override
	public String getPassword() {
		return user.getPassword();
	}

	/****
		 * Returns the user's email address to be used as the username for authentication.
		 *
		 * @return the user's email
		 */
	@Override
	public String getUsername() {
		return user.getEmail();
	}
}
