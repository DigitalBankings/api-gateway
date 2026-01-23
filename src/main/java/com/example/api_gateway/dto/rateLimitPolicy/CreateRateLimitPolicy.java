package com.example.api_gateway.dto.rateLimitPolicy;

import com.example.api_gateway.enums.Status;
import com.example.api_gateway.modles.GatewayRateLimitPolicy;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateRateLimitPolicy {

  @NotNull private Long id;
  private Integer replenishRate;
  private Integer burstCapacity;
  private Integer burstRate;
  private Integer windowSeconds;
  private String keyResolverPolicyId;
  private Status status;
  private LocalDateTime createdAt = LocalDateTime.now();
  private LocalDateTime updatedAt = LocalDateTime.now();

  public GatewayRateLimitPolicy toEntity() {
    GatewayRateLimitPolicy gatewayRateLimitPolicy = new GatewayRateLimitPolicy();
    gatewayRateLimitPolicy.setId(UUID.randomUUID().toString());
    gatewayRateLimitPolicy.setReplenishRate(replenishRate);
    gatewayRateLimitPolicy.setBurstCapacity(burstCapacity);
    gatewayRateLimitPolicy.setWindowSeconds(windowSeconds);
    gatewayRateLimitPolicy.setKeyResolverPolicyId(keyResolverPolicyId);
    gatewayRateLimitPolicy.setStatus(Status.ACTIVE);
    gatewayRateLimitPolicy.setCreatedAt(createdAt);
    gatewayRateLimitPolicy.setUpdatedAt(updatedAt);
    return gatewayRateLimitPolicy;

  }
}
