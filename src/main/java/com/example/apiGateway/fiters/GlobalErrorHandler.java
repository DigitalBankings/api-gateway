package com.example.apiGateway.fiters;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import java.net.ConnectException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Order(-2) // run before default handler
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

  private final ObjectMapper mapper = new ObjectMapper();

  @Override
  public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {

    // Prevent double response writing
    if (exchange.getResponse().isCommitted()) {
      return Mono.error(ex);
    }

    HttpStatus status;
    String message;

    // Unwrap root cause
    Throwable cause = ex;
    while (cause.getCause() != null) {
      cause = cause.getCause();
    }

    /* ---------- STATUS MAPPING ---------- */

    if (cause instanceof TimeoutException) {
      // Service slow
      status = HttpStatus.GATEWAY_TIMEOUT; // 504
      message = "Service response exceeded timeout limit";

    } else if (cause instanceof ConnectException) {
      // Service down
      status = HttpStatus.SERVICE_UNAVAILABLE; // 503
      message = "Service temporarily unavailable";

    } else if (cause instanceof CallNotPermittedException) {
      // Circuit breaker open
      status = HttpStatus.SERVICE_UNAVAILABLE; // 503
      message = "Service unavailable due to high load";

    } else if (ex instanceof ResponseStatusException rse) {
      // Preserve existing HTTP status (VERY IMPORTANT)
      status = HttpStatus.valueOf(rse.getStatusCode().value());
      message = rse.getReason() != null ? rse.getReason() : status.getReasonPhrase();

    } else {
      // Unknown error
      status = HttpStatus.INTERNAL_SERVER_ERROR; // 500
      message = "Unexpected gateway error" + ex.getMessage();
    }

    /* ---------- RESPONSE BODY ---------- */

    exchange.getResponse().setStatusCode(status);
    exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

    Map<String, Object> body = new HashMap<>();
    body.put("errorCode", status.name());
    body.put("message", message);
    body.put("path", exchange.getRequest().getPath().value());
    exchange.getRequest().getMethod();
    body.put("method", exchange.getRequest().getMethod().name());
    body.put("status", status.value());
    body.put("timestamp", Instant.now().toString());

    byte[] bytes;
    try {
      bytes = mapper.writeValueAsBytes(body);
    } catch (Exception e) {
      bytes = "{}".getBytes();
    }

    return exchange
        .getResponse()
        .writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
  }
}
