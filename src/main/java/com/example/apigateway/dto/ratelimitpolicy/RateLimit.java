package com.example.apigateway.dto.ratelimitpolicy;

import lombok.Builder;
import lombok.Data;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;

@Data
@Builder
public class RateLimit {
    private Integer replenishRate;
    private Integer burstCapacity;
    private Integer windowSeconds;
    private KeyResolver keyResolver;
}
