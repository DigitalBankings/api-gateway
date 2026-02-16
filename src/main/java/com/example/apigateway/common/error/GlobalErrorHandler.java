package com.example.apigateway.common.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Order(-2)
@RequiredArgsConstructor
@Slf4j
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

  private final ObjectMapper objectMapper;

  @Override
  public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {

    if (exchange.getResponse().isCommitted()) {
      return Mono.error(ex);
    }

    String traceId = (String) exchange.getAttribute("traceId");
    if (traceId == null) {
      traceId = "unknown";
    }

    Throwable root = unwrap(ex);
    GatewayErrorCode errorCode = mapError(root);
    String message = resolveMessage(root, errorCode);

    GatewayErrorResponse response =
        GatewayErrorResponse.builder()
            .errorCode(errorCode.getCode())
            .message(message)
            .status(errorCode.getStatus().value())
            .path(exchange.getRequest().getPath().value())
            .method(exchange.getRequest().getMethod().name())
            .traceId(traceId)
            .timestamp(LocalDateTime.now())
            .build();

    exchange.getResponse().setStatusCode(errorCode.getStatus());
    exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

    String finalTraceId = traceId;
    return exchange
        .getResponse()
        .writeWith(
            Mono.fromSupplier(
                () -> {
                  try {
                    byte[] bytes = objectMapper.writeValueAsBytes(response);
                    return exchange.getResponse().bufferFactory().wrap(bytes);
                  } catch (Exception e) {
                    log.error("Failed to serialize error response. traceId={}", finalTraceId, e);
                    return exchange.getResponse().bufferFactory().wrap("{}".getBytes());
                  }
                }));
  }

  /** Error Mapping */
  private GatewayErrorCode mapError(Throwable ex) {

    if (ex instanceof BusinessException bex) {
      return bex.getErrorCode();
    }

    if (ex instanceof MethodArgumentNotValidException) {
      return GatewayErrorCode.VALIDATION_ERROR;
    }

    if (ex instanceof ConstraintViolationException) {
      return GatewayErrorCode.VALIDATION_ERROR;
    }

    if (ex instanceof ResponseStatusException rse) {
      if (rse.getStatusCode().is4xxClientError()) {
        return GatewayErrorCode.BAD_REQUEST;
      }
    }

    if (ex instanceof TimeoutException) {
      return GatewayErrorCode.SERVICE_TIMEOUT;
    }

    if (ex instanceof CallNotPermittedException) {
      return GatewayErrorCode.CIRCUIT_OPEN;
    }

    return GatewayErrorCode.INTERNAL_ERROR;
  }

  private String resolveMessage(Throwable ex, GatewayErrorCode code) {

    if (ex instanceof BusinessException bex) {
      return bex.getMessage();
    }

    if (ex instanceof MethodArgumentNotValidException manv) {
      return manv.getBindingResult().getFieldErrors().stream()
          .map(e -> e.getField() + " " + e.getDefaultMessage())
          .findFirst()
          .orElse(code.getDefaultMessage());
    }

    if (ex instanceof ConstraintViolationException cve) {
      return cve.getConstraintViolations().stream()
          .map(v -> v.getPropertyPath() + " " + v.getMessage())
          .findFirst()
          .orElse(code.getDefaultMessage());
    }

    return code.getDefaultMessage();
  }

  private Throwable unwrap(Throwable ex) {
    Throwable result = ex;
    while (result.getCause() != null && result.getCause() != result) {
      result = result.getCause();
    }
    return result;
  }
}
