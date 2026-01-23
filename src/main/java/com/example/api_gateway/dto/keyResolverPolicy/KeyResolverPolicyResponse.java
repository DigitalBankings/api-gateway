package com.example.api_gateway.dto.keyResolverPolicy;

import com.example.api_gateway.enums.KeyResolverStrategy;
import com.example.api_gateway.enums.Status;
import com.example.api_gateway.modles.GatewayKeyResolverPolicy;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KeyResolverPolicyResponse {
  private Long id;
  private String policyCode;
  private KeyResolverStrategy strategy;
  private String headerName;
  private KeyResolverStrategy fallbackStrategy;
  private Status status;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public static KeyResolverPolicyResponse fromEntity(
      GatewayKeyResolverPolicy gatewayKeyResolverPolicy) {
    KeyResolverPolicyResponse keyResolverPolicyResponse = new KeyResolverPolicyResponse();
    keyResolverPolicyResponse.setId(gatewayKeyResolverPolicy.getId());
    keyResolverPolicyResponse.setPolicyCode(gatewayKeyResolverPolicy.getPolicyCode());
    keyResolverPolicyResponse.setStrategy(gatewayKeyResolverPolicy.getStrategy());
    keyResolverPolicyResponse.setHeaderName(gatewayKeyResolverPolicy.getHeaderName());
    keyResolverPolicyResponse.setFallbackStrategy(gatewayKeyResolverPolicy.getFallbackStrategy());
    keyResolverPolicyResponse.setStatus(gatewayKeyResolverPolicy.getStatus());
    keyResolverPolicyResponse.setCreatedAt(gatewayKeyResolverPolicy.getCreatedAt());
    keyResolverPolicyResponse.setUpdatedAt(gatewayKeyResolverPolicy.getUpdatedAt());
    return keyResolverPolicyResponse;
  }
}
