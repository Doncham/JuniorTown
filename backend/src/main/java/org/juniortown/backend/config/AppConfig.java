package org.juniortown.backend.config;

import java.util.Base64;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "stephen")
public class AppConfig {
	public String curry;
	private byte[] jwtKey;

	public void setJwtKey(String jwtKey) {
		this.jwtKey = Base64.getDecoder().decode(jwtKey);
	}

}
