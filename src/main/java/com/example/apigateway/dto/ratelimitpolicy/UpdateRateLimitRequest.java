package com.example.apigateway.dto.ratelimitpolicy;

import com.example.apigateway.enums.Status;
import com.example.apigateway.modles.GatewayRateLimitPolicy;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateRateLimitRequest {
  private Long id;
  private Integer replenishRate;
  private Integer burstCapacity;
  private Integer windowSeconds;
  private Long keyResolverPolicyId;
  private Status status;

  public void updateEntity(GatewayRateLimitPolicy gatewayRateLimitPolicy) {
    gatewayRateLimitPolicy.setReplenishRate(replenishRate);
    gatewayRateLimitPolicy.setBurstCapacity(burstCapacity);
    gatewayRateLimitPolicy.setWindowSeconds(windowSeconds);
    gatewayRateLimitPolicy.setKeyResolverPolicyId(keyResolverPolicyId);
    gatewayRateLimitPolicy.setStatus(status);
  }
}
