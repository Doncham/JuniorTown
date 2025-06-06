package org.juniortown.backend.config;

import java.util.Optional;

import org.juniortown.backend.config.data.UserSession;
import org.juniortown.backend.domain.Session;
import org.juniortown.backend.exception.Unauthorized;
import org.juniortown.backend.repository.SessionRepository;
import org.juniortown.backend.response.SessionResponse;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class AuthResolver implements HandlerMethodArgumentResolver {
	private final SessionRepository sessionRepository;
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.getParameterType().equals(UserSession.class);
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
		NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
		if (servletRequest == null) {
			log.error("servletRequest null");
			throw new Unauthorized();
		}
		Cookie[] cookies = servletRequest.getCookies();
		if (cookies.length == 0) {
			log.error("쿠키 없음");
			throw new Unauthorized();
		}
		String accessToken = cookies[0].getValue();

		Session session = sessionRepository.findByAccessToken(accessToken)
			.orElseThrow(Unauthorized::new);

		// DB를 뒤져서 PK를 넣어줌
		return new UserSession(session.getUser().getId());
	}
}
