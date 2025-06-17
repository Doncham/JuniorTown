package org.juniortown.backend.user.service;

import java.util.Optional;

import org.juniortown.backend.user.dto.CustomUserDetails;
import org.juniortown.backend.user.entity.User;
import org.juniortown.backend.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
	private final UserRepository userRepository;
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		Optional<User> userData = userRepository.findByEmail(email);

		if (userData.isPresent()) {
			return new CustomUserDetails(userData.get());
		}

		throw new UsernameNotFoundException("User not found with email: " + email);
	}
}
