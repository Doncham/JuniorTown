package org.juniortown.backend.config;

import org.juniortown.backend.config.data.UserSession;
import org.juniortown.backend.exception.Unauthorized;
import org.juniortown.backend.repository.SessionRepository;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class AuthResolver implements HandlerMethodArgumentResolver {
	private final SessionRepository sessionRepository;
	private final AppConfig appConfig;
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.getParameterType().equals(UserSession.class);
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
		NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		log.info(">>>>>>>>{}", appConfig);
		String jws = webRequest.getHeader("Authorization");
		if (jws == null) {
			log.error("jws null");
			throw new Unauthorized();
		}

		try {
			Jws<Claims> claims = Jwts.parserBuilder()
				.setSigningKey(appConfig.getJwtKey())
				.build()
				.parseClaimsJws(jws);
			String userId = claims.getBody().getSubject();
			return new UserSession(Long.parseLong(userId));
		} catch (JwtException e) {
			throw new Unauthorized();
		}



		// DB를 뒤져서 PK를 넣어줌
		//return new UserSession(session.getUser().getId());
	}
}
