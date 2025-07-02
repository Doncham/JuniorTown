package org.juniortown.backend.user.jwt;

import java.io.IOException;

import org.juniortown.backend.user.dto.CustomUserDetails;
import org.juniortown.backend.user.entity.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class JWTFilter extends OncePerRequestFilter {
	private final JWTUtil jwtUtil;
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {

		String authorization = request.getHeader("Authorization");

		if (authorization == null || !authorization.startsWith("Bearer ")) {
			log.warn("requestURI: " + request.getRequestURI() + " message: token null");
			filterChain.doFilter(request, response);
			return;
		}

		// 인증 시작
		String token = authorization.split(" ")[1];

		//토큰 소멸 시간 검증
		if (jwtUtil.isExpired(token)) {
			System.out.println("token expired");
			filterChain.doFilter(request, response);
			return;
		}

		String username = jwtUtil.getUsername(token);
		String role = jwtUtil.getRole(token);
		Long userId = jwtUtil.getUserId(token);

		//userEntity를 생성하여 값 set
		User user = User.builder()
			.id(userId)
			.email(username)
			.role(role)
			.build();

		CustomUserDetails customUserDetails = new CustomUserDetails(user);
		UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
			customUserDetails, null, customUserDetails.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authToken);

		filterChain.doFilter(request, response);
	}
}
