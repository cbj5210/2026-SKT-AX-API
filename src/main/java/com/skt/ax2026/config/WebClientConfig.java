package com.skt.ax2026.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.LoggingCodecSupport;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;


@Slf4j
@Configuration
public class WebClientConfig {

	private static final int MAX_IN_MEMORY_SIZE = 1024 * 1024 * 5; // 5MB (default : 256k, unlimited : -1)
	private static final int CONNECT_TIMEOUT = 5888; // 5s
	private static final int READ_TIMEOUT = 60888;   // 60s
	private static final String CHARSET = "UTF-8";

	@Bean
	public WebClient webClient() {
		ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
		                                                          .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(MAX_IN_MEMORY_SIZE))
		                                                          .build();

		exchangeStrategies.messageWriters()
		                  .stream()
		                  .filter(LoggingCodecSupport.class::isInstance)
		                  .forEach(writer -> ((LoggingCodecSupport) writer).setEnableLoggingRequestDetails(true));

		return WebClient.builder()
		                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		                .defaultHeader(HttpHeaders.ACCEPT_CHARSET, CHARSET)
		                .clientConnector(new ReactorClientHttpConnector(HttpClient.create()
		                                                                          .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT)
		                                                                          .doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(READ_TIMEOUT))
		                                                                                                     .addHandlerLast(new WriteTimeoutHandler(READ_TIMEOUT))
		                                                                          )
		                                 )
		                )
		                .exchangeStrategies(exchangeStrategies)
		                .filter(ExchangeFilterFunction.ofRequestProcessor(
		                  clientRequest -> {
			                  log.debug("[REQUEST] method = {}, url = {}", clientRequest.method(), clientRequest.url());
			                  clientRequest.headers().forEach((name, values) -> values.forEach(value -> log.debug("[Request Header] {} : {}", name, value)));
			                  return Mono.just(clientRequest);
		                  })
		                )
		                .filter(ExchangeFilterFunction.ofResponseProcessor(
		                  clientResponse -> {
			                  clientResponse.headers()
			                                .asHttpHeaders()
			                                .forEach((name, values) -> values.forEach(value -> log.debug("[Response Header] {} : {}", name, value)));
			                  return Mono.just(clientResponse);
		                  }
		                ))
		                .build();
	}
}

