package com.example.apiGateway.fiters;


import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class PostLoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        long startTime = System.currentTimeMillis();

        return chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    long duration = System.currentTimeMillis() - startTime;
                    System.out.println("POST FILTER → Status: " +
                            exchange.getResponse().getStatusCode() +
                            ", Duration: " + duration + "ms");
                }));
    }

    @Override
    public int getOrder() {
        return 1; // Run after pre-filters
    }
}
