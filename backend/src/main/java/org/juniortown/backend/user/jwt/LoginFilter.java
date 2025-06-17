package org.juniortown.backend.user.jwt;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.juniortown.backend.user.dto.CustomUserDetails;
import org.juniortown.backend.user.dto.LoginDTO;
import org.juniortown.backend.user.response.LoginResultDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class LoginFilter extends UsernamePasswordAuthenticationFilter {
	private final AuthenticationManager authenticationManager;
	private final JWTUtil jwtUtil;


	/**
	 * Attempts to authenticate a user by reading login credentials from the HTTP request body.
	 *
	 * Parses the request body as JSON to extract the user's email and password, then delegates authentication to the authentication manager.
	 *
	 * @param request the HTTP request containing login credentials in JSON format
	 * @param response the HTTP response
	 * @return the authenticated Authentication object if credentials are valid
	 * @throws AuthenticationException if authentication fails or if the request body cannot be parsed as valid JSON
	 */
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws
		AuthenticationException {
		try {
			LoginDTO loginDTO = new ObjectMapper().readValue(request.getInputStream(), LoginDTO.class);
			UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword(), null);
			return authenticationManager.authenticate(authToken);
		} catch (IOException e) {
			throw new AuthenticationServiceException("JSON parsing error", e);
		}
	}

	/**
	 * Handles actions upon successful user authentication, including JWT token issuance.
	 *
	 * Generates a JWT token valid for 24 hours using the authenticated user's username and role, adds it to the response header, and writes a JSON login success message to the response body with HTTP status 200.
	 */
	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) {
		CustomUserDetails customUserDetails = (CustomUserDetails)authentication.getPrincipal();
		String username = customUserDetails.getUsername();

		Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
		Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
		GrantedAuthority auth = iterator.next();

		String role = auth.getAuthority();

		String token = jwtUtil.createJwt(username, role, 60 * 60 * 24L); // 24시간 유효한 토큰 생성

		response.addHeader("Authorization", "Bearer " + token);

		LoginResultDTO loginSuccessful = LoginResultDTO.builder()
			.message("Login successful")
			.build();

		//response에 로그인 성공 메시지 추가
		try {
			response.setContentType("application/json");
			response.getWriter().write(new ObjectMapper().writeValueAsString(loginSuccessful));
			response.setStatus(HttpStatus.OK.value());
		} catch (IOException e) {
			log.error("Error writing response: {}", e.getMessage());
		}
	}

	/**
	 * Handles actions to perform when user authentication fails during login.
	 *
	 * Sets the HTTP response status to 401 (Unauthorized) and writes a JSON-formatted failure message to the response body.
	 */
	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
		log.error("Login failed: {}", failed.getMessage());
		LoginResultDTO loginFailed = LoginResultDTO.builder()
			.message("Login failed")
			.build();
		response.setStatus(401);

		try {
			response.setContentType("application/json");
			response.getWriter().write(new ObjectMapper().writeValueAsString(loginFailed));
		} catch (IOException e) {
			log.error("Error writing response: {}", e.getMessage());
		}
	}
}
