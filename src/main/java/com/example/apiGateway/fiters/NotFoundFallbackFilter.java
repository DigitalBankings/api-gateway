package com.example.apiGateway.fiters;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class NotFoundFallbackFilter implements GlobalFilter, Ordered {

  private final ObjectMapper mapper = new ObjectMapper();

  private static final Set<String> ALLOWED_PATHS =
      Set.of(
          "/api/system/oauth2/authorize",
          "/api/system/oauth2/authn",
          "/api/system/oauth2/token",
          "/api/system/oauth2/callback");

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    String path = exchange.getRequest().getPath().value();

    if (!ALLOWED_PATHS.contains(path)) {
      exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
      exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
      Map<String, Object> response = new HashMap<>();
      response.put("errorCode", "NOT_FOUND");
      response.put("message", "API not available or not subscribed");
      response.put("path", path);
      response.put("method", exchange.getRequest().getMethod().name());
      response.put("status", 404);
      byte[] bytes;
      try {
        bytes = mapper.writeValueAsBytes(response);
      } catch (Exception e) {
        bytes = "{\"error\":\"serialization error\"}".getBytes();
      }

      return exchange
          .getResponse()
          .writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
    }

    return chain.filter(exchange);
  }

  @Override
  public int getOrder() {
    return 1000; // after pre/post filters
  }
}
