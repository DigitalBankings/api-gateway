package com.example.apiGateway.dto.keyResolverPolicy;

import com.example.apiGateway.enums.KeyResolverStrategy;
import com.example.apiGateway.enums.Status;
import com.example.apiGateway.modles.GatewayKeyResolverPolicy;
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
    gatewayKeyResolverPolicy.setId(UUID.randomUUID().toString());
    gatewayKeyResolverPolicy.setPolicyCode(policyCode);
    gatewayKeyResolverPolicy.setStrategy(strategy);
    gatewayKeyResolverPolicy.setHeaderName(headerName);
    gatewayKeyResolverPolicy.setFallbackStrategy(fallbackStrategy);
    gatewayKeyResolverPolicy.setStatus(Status.valueOf(status.name()));
    return gatewayKeyResolverPolicy;
  }
}
