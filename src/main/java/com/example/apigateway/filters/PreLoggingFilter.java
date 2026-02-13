package com.example.apigateway.filters;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;


@Slf4j
@Component
public class PreLoggingFilter implements GlobalFilter, Ordered {

  @Override
  public Mono<Void> filter(ServerWebExchange exchange,
                           org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {

    ServerHttpRequest request = exchange.getRequest();

    log.info("PRE FILTER → Method: {}, Path: {}, Header: {}",
            request.getMethod(),
            request.getPath(),
            request.getHeaders().getFirst("Authorization"));

    ServerHttpRequest mutatedRequest = request.mutate()
            .header("X-Correlation-ID", UUID.randomUUID().toString())
            .build();

    return chain.filter(exchange.mutate().request(mutatedRequest).build());
  }

  @Override
  public int getOrder() {
    return -1;
  }
}
