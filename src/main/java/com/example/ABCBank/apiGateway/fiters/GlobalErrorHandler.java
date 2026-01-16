package com.example.ABCBank.apiGateway.fiters;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
@Order(-2)
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status;
        String message;

        if (ex instanceof ResponseStatusException rse) {
            // Spring's built-in status exception
            status = (HttpStatus) rse.getStatusCode();
            message = rse.getReason() != null ? rse.getReason() : "Unexpected error";
        } else if (ex instanceof WebClientResponseException wcre) {
            // Downstream service failed
            status = HttpStatus.valueOf(wcre.getRawStatusCode());
            message = "Downstream service error: " + wcre.getResponseBodyAsString();
        } else {
            // Fallback for unexpected errors
            status = HttpStatus.SERVICE_UNAVAILABLE; // 503
            message = "Service temporarily unavailable: " + ex.getMessage();
        }

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("errorCode", status.name());
        body.put("message", message);
        body.put("path", exchange.getRequest().getPath().value());
        body.put("status", status.value());
        body.put("timestamp", Instant.now().toString());

        byte[] bytes;
        try {
            bytes = mapper.writeValueAsBytes(body);
        } catch (Exception e) {
            bytes = "{\"error\":\"serialization error\"}".getBytes();
        }

        return exchange.getResponse()
                .writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
    }
}
