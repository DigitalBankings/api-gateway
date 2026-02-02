package com.example.apigateway.exceptionhandle;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@Order(-2) // before default handler
public class GatewayGlobalErrorHandler implements ErrorWebExceptionHandler {

  private final ObjectMapper mapper = new ObjectMapper();

  @Override
  public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {

    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

    if (ex instanceof ResponseStatusException rse) {
      status = HttpStatus.valueOf(rse.getStatusCode().value());
    }

    // We only customize NOT FOUND (no route)
    if (status == HttpStatus.NOT_FOUND) {
      exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
      exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

      Map<String, Object> body = new HashMap<>();
      body.put("errorCode", "ROUTE_NOT_FOUND");
      body.put("message", "No API route configured in Gateway");
      body.put("path", exchange.getRequest().getPath().value());
      body.put("method", exchange.getRequest().getMethod().name());
      body.put("status", 404);

      byte[] bytes;
      try {
        bytes = mapper.writeValueAsBytes(body);
      } catch (Exception e) {
        bytes = "{\"error\":\"serialization error\"}".getBytes(StandardCharsets.UTF_8);
      }

      return exchange
          .getResponse()
          .writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
    }

    return Mono.error(ex);
  }
}
