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

	//로그인 성공시 실행하는 메소드 (여기서 JWT를 발급하면 됨)
	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) {
		CustomUserDetails customUserDetails = (CustomUserDetails)authentication.getPrincipal();
		String username = customUserDetails.getUsername();

		Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
		Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
		GrantedAuthority auth = iterator.next();

		String role = auth.getAuthority();

		String token = jwtUtil.createJwt(username, role, 1000 * 60 * 60 * 24L ); // 24시간 유효한 토큰 생성

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

	//로그인 실패시 실행하는 메소드
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
