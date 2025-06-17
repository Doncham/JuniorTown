package org.juniortown.backend.user.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;

@Component
public class JWTUtil {

	private final SecretKey secretKey;

	/**
	 * Constructs a JWTUtil instance with a secret key for signing and verifying JWT tokens.
	 *
	 * @param secret the secret string used to generate the signing key
	 */
	public JWTUtil(@Value("${spring.jwt.secret}")String secret) {
		secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
	}

	/**
	 * Extracts the "email" claim from a JWT token.
	 *
	 * @param token the JWT token string
	 * @return the email address contained in the token's "email" claim
	 */
	public String getUsername(String token) {
		return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("email", String.class);
	}

	/**
	 * Extracts the "role" claim from a JWT token after verifying its signature.
	 *
	 * @param token the JWT token string
	 * @return the value of the "role" claim in the token payload
	 */
	public String getRole(String token) {
		return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class);
	}

	/**
	 * Determines whether the provided JWT token is expired.
	 *
	 * @param token the JWT token to check
	 * @return true if the token's expiration date is before the current time; false otherwise
	 */
	public Boolean isExpired(String token) {
		return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date());
	}

	/****
	 * Creates a signed JWT token containing the specified email and role claims, with a set expiration time.
	 *
	 * @param email the email to include as the "email" claim
	 * @param role the role to include as the "role" claim
	 * @param expiredMs the token's validity duration in milliseconds from the current time
	 * @return a compact JWT string signed with the configured secret key
	 */
	public String createJwt(String email, String role, Long expiredMs) {

		return Jwts.builder()
			.claim("email", email)
			.claim("role", role)
			.issuedAt(new Date(System.currentTimeMillis()))
			.expiration(new Date(System.currentTimeMillis() + expiredMs))
			.signWith(secretKey)
			.compact();
	}
}