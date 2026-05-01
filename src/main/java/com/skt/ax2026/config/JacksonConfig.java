package com.skt.ax2026.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

	@Bean
	public ObjectMapper objectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();

		// Java 8 날짜/시간 모듈 등록 (LocalDateTime 등을 위해 필수)
		objectMapper.registerModule(new JavaTimeModule());

		// 타임스탬프를 숫자가 아닌 ISO-8601 문자열로 출력 (선택)
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		// 모르는 JSON 속성이 객체에 없어도 에러내지 않음 (권장)
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		return objectMapper;
	}
}