package com.example.apigateway.dto.ratelimitpolicy;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RateLimitDTO {
    private int replenishRate;
    private int burstCapacity;
    private int windowSeconds;
    private String keyResolver;
}
