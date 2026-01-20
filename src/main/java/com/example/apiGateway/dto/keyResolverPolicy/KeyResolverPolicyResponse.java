package com.example.apiGateway.dto.keyResolverPolicy;


import com.example.apiGateway.enums.KeyResolverStrategy;
import com.example.apiGateway.enums.Status;
import com.example.apiGateway.modles.GatewayKeyResolverPolicy;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KeyResolverPolicyResponse {
    private String id;
    private String policyCode;
    private KeyResolverStrategy strategy;
    private String headerName;
    private KeyResolverStrategy fallbackStrategy;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static KeyResolverPolicyResponse fromEntity(GatewayKeyResolverPolicy gatewayKeyResolverPolicy) {
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
