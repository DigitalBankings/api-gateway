package com.example.apigateway.fiters;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class PostLoggingFilter implements GlobalFilter, Ordered {

  @Override
  public Mono<Void> filter(
      ServerWebExchange exchange,
      org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
    long startTime = System.currentTimeMillis();

    return chain
        .filter(exchange)
        .then(
            Mono.fromRunnable(
                () -> {
                  long duration = System.currentTimeMillis() - startTime;
                    log.info("POST FILTER → Status: {}, Duration: {} ms",
                            exchange.getResponse().getStatusCode(),
                            duration);
                                  }));
  }

  @Override
  public int getOrder() {
    return 1; // Run after pre-filters
  }
}
