package com.example.apigateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimitConfig {

  @Bean("ipKeyResolver")
  @Primary
  public KeyResolver ipKeyResolver() {
    return exchange ->
        Mono.just(exchange.getRequest().getRemoteAddress().getAddress().getHostAddress());
  }

  @Bean("clientKeyResolver")
  public KeyResolver clientKeyResolver() {
    return exchange ->
        Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst("X-F"))
            .defaultIfEmpty("anonymous");
  }
}
