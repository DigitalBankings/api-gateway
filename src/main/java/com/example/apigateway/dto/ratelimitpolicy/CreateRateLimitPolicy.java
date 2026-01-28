package com.example.apigateway.dto.ratelimitpolicy;

import com.example.apigateway.enums.Status;
import com.example.apigateway.modles.GatewayRateLimitPolicy;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateRateLimitPolicy {

  private Integer replenishRate;
  private Integer burstCapacity;
  private Integer burstRate;
  private Integer windowSeconds;
  private Long keyResolverPolicyId;
  private Status status;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public GatewayRateLimitPolicy toEntity() {
    GatewayRateLimitPolicy gatewayRateLimitPolicy = new GatewayRateLimitPolicy();
    gatewayRateLimitPolicy.setReplenishRate(replenishRate);
    gatewayRateLimitPolicy.setBurstCapacity(burstCapacity);
    gatewayRateLimitPolicy.setWindowSeconds(windowSeconds);
    gatewayRateLimitPolicy.setKeyResolverPolicyId(keyResolverPolicyId);
    gatewayRateLimitPolicy.setStatus(Status.ACTIVE);
    gatewayRateLimitPolicy.setCreatedAt(LocalDateTime.now());
    gatewayRateLimitPolicy.setUpdatedAt(LocalDateTime.now());
    return gatewayRateLimitPolicy;
  }
}
