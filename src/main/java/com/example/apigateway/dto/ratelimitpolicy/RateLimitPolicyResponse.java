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
public class RateLimitPolicyResponse {

  private String id;
  private Integer replenishRate;
  private Integer burstCapacity;
  private Integer windowSeconds;
  private String keyResolverPolicyId;
  private Status status;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public static RateLimitPolicyResponse fromEntity(GatewayRateLimitPolicy gatewayRateLimitPolicy) {
    RateLimitPolicyResponse rateLimitPolicyResponse = new RateLimitPolicyResponse();
    rateLimitPolicyResponse.setId(gatewayRateLimitPolicy.getId());
    rateLimitPolicyResponse.setReplenishRate(gatewayRateLimitPolicy.getReplenishRate());
    rateLimitPolicyResponse.setBurstCapacity(gatewayRateLimitPolicy.getBurstCapacity());
    rateLimitPolicyResponse.setWindowSeconds(gatewayRateLimitPolicy.getWindowSeconds());
    rateLimitPolicyResponse.setKeyResolverPolicyId(gatewayRateLimitPolicy.getKeyResolverPolicyId());
    rateLimitPolicyResponse.setStatus(gatewayRateLimitPolicy.getStatus());
    rateLimitPolicyResponse.setCreatedAt(gatewayRateLimitPolicy.getCreatedAt());
    rateLimitPolicyResponse.setUpdatedAt(gatewayRateLimitPolicy.getUpdatedAt());
    return rateLimitPolicyResponse;
  }
}
