package com.example.api_gateway.dto.keyResolverPolicy;

import com.example.api_gateway.enums.KeyResolverStrategy;
import com.example.api_gateway.enums.Status;
import com.example.api_gateway.modles.GatewayKeyResolverPolicy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateKeyResolverPolicyRequest {

  @NotBlank private String policyCode;

  @NotNull private KeyResolverStrategy strategy;
  private String headerName;
  private KeyResolverStrategy fallbackStrategy;
  private Status status = Status.ACTIVE;

  public GatewayKeyResolverPolicy toEntity() {
    GatewayKeyResolverPolicy gatewayKeyResolverPolicy = new GatewayKeyResolverPolicy();
    gatewayKeyResolverPolicy.setPolicyCode("PHL" + "-" + UUID.randomUUID().toString().substring(0, 8));
    gatewayKeyResolverPolicy.setStrategy(strategy);
    gatewayKeyResolverPolicy.setHeaderName(headerName);
    gatewayKeyResolverPolicy.setFallbackStrategy(fallbackStrategy);
    gatewayKeyResolverPolicy.setStatus(Status.valueOf(status.name()));
    return gatewayKeyResolverPolicy;
  }
}
