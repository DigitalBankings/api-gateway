package com.example.apiGateway.fiters;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class PreLoggingFilter implements GlobalFilter, Ordered {

  @Override
  public Mono<Void> filter(
      ServerWebExchange exchange,
      org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
    // Fix for getMethod
    String method =
        exchange.getRequest().getMethod() != null
            ? exchange.getRequest().getMethod().name()
            : "UNKNOWN";
    String path = exchange.getRequest().getPath().value();

    String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

    System.out.println(
        "PRE FILTER → Method: " + method + ", Path: " + path + ", Authorization: " + authHeader);

    // Add correlation header
    exchange
        .getRequest()
        .mutate()
        .header("X-Correlation-ID", java.util.UUID.randomUUID().toString())
        .build();

    return chain.filter(exchange);
  }

  @Override
  public int getOrder() {
    return -1;
  }
}
