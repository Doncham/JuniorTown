package org.juniortown.backend.config;

import java.util.List;

import org.juniortown.backend.repository.SessionRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
	private final SessionRepository sessionRepository;
	private final AppConfig appConfig;

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {

		resolvers.add(new AuthResolver(sessionRepository, appConfig));
	}
}
