package org.juniortown.backend.config;

import java.util.Collections;

import org.juniortown.backend.user.jwt.JWTFilter;
import org.juniortown.backend.user.jwt.JWTUtil;
import org.juniortown.backend.user.jwt.LoginFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	private final AuthenticationConfiguration authenticationConfiguration;
	private final JWTUtil jwtUtil;

	/****
	 * Provides the `AuthenticationManager` bean for authentication operations.
	 *
	 * @param configuration the authentication configuration used to obtain the manager
	 * @return the configured `AuthenticationManager` instance
	 * @throws Exception if the authentication manager cannot be retrieved
	 */
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}

	/****
	 * Provides a BCryptPasswordEncoder bean for password hashing.
	 *
	 * @return a BCryptPasswordEncoder instance
	 */
	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/****
	 * Configures the application's security filter chain, including CORS, CSRF, authentication, authorization, and custom filters.
	 *
	 * Sets up CORS to allow requests from "http://localhost:3000" with all methods and headers, enables credential support, and exposes the "Authorization" header. Disables CSRF protection, form login, and HTTP Basic authentication. Defines authorization rules to permit access to login, signup, and root endpoints, restrict "/admin" to users with the "ADMIN" role, and require authentication for all other endpoints. Registers a custom login filter for handling authentication at "/api/auth/login" and a JWT filter for validating tokens. Configures session management to be stateless.
	 *
	 * @param http the HttpSecurity to configure
	 * @return the configured SecurityFilterChain
	 * @throws Exception if an error occurs during configuration
	 */
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.cors((corsCustomizer) -> corsCustomizer.configurationSource(new CorsConfigurationSource() {
				@Override
				public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
					CorsConfiguration configuration = new CorsConfiguration();
					configuration.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));
					configuration.setAllowedMethods(Collections.singletonList("*"));
					configuration.setAllowCredentials(true);
					configuration.setAllowedHeaders(Collections.singletonList("*"));
					configuration.setMaxAge(3600L);

					configuration.setExposedHeaders(Collections.singletonList("Authorization"));

					return configuration;
				}
			}));
		//csrf disable
		http
			.csrf((auth) -> auth.disable());

		//From 로그인 방식 disable
		http
			.formLogin((auth) -> auth.disable());

		//http basic 인증 방식 disable
		http
			.httpBasic((auth) -> auth.disable());

		//경로별 인가 작업
		http
			.authorizeHttpRequests((auth) -> auth
				.requestMatchers("api/auth/login", "/", "/api/auth/signup").permitAll()
				.requestMatchers("/admin").hasRole("ADMIN")
				.anyRequest().authenticated());

		LoginFilter loginFilter = new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil);
		loginFilter.setFilterProcessesUrl("/api/auth/login");

		http.addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class);
		http.addFilterBefore(new JWTFilter(jwtUtil), LoginFilter.class);
        		//JWT 필터 설정
		//세션 설정
		http
			.sessionManagement((session) -> session
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		return http.build();
	}
}