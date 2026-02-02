package com.example.apigateway.fiters;

import com.example.apigateway.services.caches.GatewayRouteStore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotFoundFallbackFilter implements GlobalFilter, Ordered {

  private final ObjectMapper mapper = new ObjectMapper();
  private final GatewayRouteStore gatewayRouteStore; // check dynamic routes

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    String path = exchange.getRequest().getPath().value();
    String method = exchange.getRequest().getMethod().name();

    // Check if any route matches
    boolean routeExists =
        gatewayRouteStore.getAllRoutes().stream()
            .anyMatch(
                route -> {
                  if (route.getPredicates() == null || route.getPredicates().getPath() == null)
                    return false;
                  boolean pathMatch = path.equals(route.getPredicates().getPath());
                  boolean methodMatch =
                      route.getPredicates().getMethod() == null
                          || method.equalsIgnoreCase(route.getPredicates().getMethod());
                  return pathMatch && methodMatch;
                });

    if (!routeExists) {
      exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
      exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

      Map<String, Object> response = new HashMap<>();
      response.put("errorCode", "ROUTE_NOT_FOUND");
      response.put("message", "No API route configured in Gateway");
      response.put("path", path);
      response.put("method", method);
      response.put("status", 404);

      byte[] bytes;
      try {
        bytes = mapper.writeValueAsBytes(response);
      } catch (JsonProcessingException e) {
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
