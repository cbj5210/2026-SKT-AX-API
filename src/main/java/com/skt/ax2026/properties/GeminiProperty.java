package com.skt.ax2026.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "gemini")
public class GeminiProperty {

	private Api api;

	@Getter
	@Setter
	public static class Api {

		private String key;
		private String url;
	}
}