package org.juniortown.backend.config;

import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class WebMvcConfig implements WebMvcConfigurer {
	/****
	 * Configures CORS mappings to allow requests from "http://localhost:3000" for all URL paths.
	 *
	 * @param registry the CORS registry to which the mapping is added
	 */
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
			.allowedOrigins("http://localhost:3000");
	}
}
