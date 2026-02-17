package com.example.apigateway.dto.keyresolverpolicy;

import com.example.apigateway.enums.KeyResolverStrategy;
import com.example.apigateway.enums.Status;
import com.example.apigateway.modles.GatewayKeyResolverPolicy;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateKeyResolverPolicyRequest {

  private Long id;
  private String policyCode;
  private KeyResolverStrategy strategy;
  private String headerName;
  private KeyResolverStrategy fallbackStrategy;
  private Status status = Status.ACTIVE;
  private LocalDateTime updatedAt;

  public void updateEntity(GatewayKeyResolverPolicy gatewayKeyResolverPolicy) {
    if (gatewayKeyResolverPolicy.getPolicyCode() != null) {
      gatewayKeyResolverPolicy.setPolicyCode(gatewayKeyResolverPolicy.getPolicyCode());
    }
    if (gatewayKeyResolverPolicy.getStrategy() != null)
      gatewayKeyResolverPolicy.setStrategy(strategy);
    if (gatewayKeyResolverPolicy.getHeaderName() != null)
      gatewayKeyResolverPolicy.setHeaderName(headerName);
    if (gatewayKeyResolverPolicy.getFallbackStrategy() != null)
      gatewayKeyResolverPolicy.setFallbackStrategy(fallbackStrategy);
    if (gatewayKeyResolverPolicy.getStatus() != null) gatewayKeyResolverPolicy.setStatus(status);
    gatewayKeyResolverPolicy.setUpdatedAt(LocalDateTime.now());
  }
}
