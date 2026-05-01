package com.skt.ax2026.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skt.ax2026.dto.GeminiRequest;
import com.skt.ax2026.dto.GeminiResponse;
import com.skt.ax2026.dto.GuideRequest;
import com.skt.ax2026.dto.GuideResponse;
import com.skt.ax2026.properties.GeminiProperty;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuideService {

	private final GeminiProperty geminiProperty;
	private final WebClient webClient;
	private final ObjectMapper objectMapper;

	public GuideResponse analyzeUiTree(GuideRequest request) {
		log.info("Received analyze request for goal: {}", request.userGoal());

		// 1. Construct Prompt
		String prompt = buildPrompt(request);

		// 2. Prepare Gemini API Request Body
		GeminiRequest requestBody = new GeminiRequest(
		  List.of(new GeminiRequest.Content(List.of(new GeminiRequest.Part(prompt)))),
		  new GeminiRequest.GenerationConfig("application/json")
		);

		// 3. Call Gemini API
		log.info("Calling Gemini API...");
		GeminiResponse response;
		try {
			response = webClient.post()
			                    .uri(geminiProperty.getApi().getUrl() + "?key=" + geminiProperty.getApi().getKey())
			                    .bodyValue(requestBody)
			                    .retrieve()
			                    .bodyToMono(GeminiResponse.class)
			                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(2)) // 최대 3번, 2초부터 지연 시간 증가하며 재시도
			                                    .filter(throwable -> throwable instanceof WebClientResponseException.TooManyRequests))
			                    .block();
		} catch (Exception e) {
			log.error("Error communicating with Gemini API", e);
			throw new RuntimeException("Failed to call Gemini API", e);
		}

		// 4. Parse Response
		try {
			if (response != null) {
				String jsonText = response.getExtractedText();
				if (jsonText != null) {
					log.info("Gemini Raw Response: {}", jsonText);
					return objectMapper.readValue(jsonText, GuideResponse.class);
				}
			}
		} catch (Exception e) {
			log.error("Error parsing Gemini response", e);
			throw new RuntimeException("Failed to parse Gemini API response", e);
		}

		throw new RuntimeException("No valid response from Gemini API");
	}

	private String buildPrompt(GuideRequest request) {
		try {
			String uiTreeJson = objectMapper.writeValueAsString(request.uiTree());

			String failedElementsStr = "";
			if (request.failedElements() != null && !request.failedElements().isEmpty()) {
				failedElementsStr = "\nIMPORTANT WARNING: The user previously tried interacting with the following elements, but they led to the WRONG screen. DO NOT suggest these elements again: "
				                      + String.join(", ", request.failedElements()) + "\n";
			}

			return String.format(
			  "You are an AI assistant helping a senior user navigate a mobile app.\n" +
				"The user's goal is: '%s'\n%s\n" +
				"Here is the current UI tree in JSON format:\n%s\n\n" +
				"Please analyze the UI tree and identify the exact button or element the user needs to click next to achieve their goal.\n" +
				"Return a JSON response with the following format EXACTLY:\n" +
				"{\n" +
				"  \"targetElement\": {\n" +
				"    \"x\": <x_coordinate>,\n" +
				"    \"y\": <y_coordinate>,\n" +
				"    \"description\": \"<description of the button>\"\n" +
				"  },\n" +
				"  \"guideMessage\": \"<friendly, easy-to-understand message in Korean to show the user>\",\n" +
				"  \"actionStatus\": \"CONTINUE\" // \"CONTINUE\", \"FINISHED\" (if goal achieved), or \"BACK\" (if on the wrong screen and needs to go back)\n" +
				"}",
			  request.userGoal(), failedElementsStr, uiTreeJson
			);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Failed to build prompt from UI tree", e);
		}
	}
}
