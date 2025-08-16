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
	private static final String CLAIM_USER_ID = "userId";
	private static final String CLAIM_EMAIL = "email";
	private static final String CLAIM_USERNAME = "username";
	private static final String CLAIM_ROLE = "role";

	public JWTUtil(@Value("${spring.jwt.secret}")String secret) {
		secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
	}

	public String getUsername(String token) {
		return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("username", String.class);
	}

	public String getUserEmail(String token) {
		return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("email", String.class);
	}

	public String getRole(String token) {
		return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class);
	}
	public Long getUserId(String token){
		return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("userId", Long.class);
	}

	public Boolean isExpired(String token) {
		return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date());
	}

	public String createJwt(Long id, String email, String username, String role, Long expiredMs) {

		return Jwts.builder()
			.claim(CLAIM_USER_ID, id)
			.claim(CLAIM_EMAIL, email)
			.claim(CLAIM_USERNAME, username)
			.claim(CLAIM_ROLE, role)
			.issuedAt(new Date(System.currentTimeMillis()))
			.expiration(new Date(System.currentTimeMillis() + expiredMs))
			.signWith(secretKey)
			.compact();
	}
}