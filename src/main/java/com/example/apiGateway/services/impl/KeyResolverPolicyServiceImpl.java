package com.example.apiGateway.services.impl;

import com.example.apiGateway.dto.keyResolverPolicy.CreateKeyResolverPolicyRequest;
import com.example.apiGateway.dto.keyResolverPolicy.KeyResolverPolicyResponse;
import com.example.apiGateway.modles.GatewayKeyResolverPolicy;
import com.example.apiGateway.repositories.GatewayKeyResolverPolicyRepository;
import com.example.apiGateway.services.KeyResolverPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor

public class KeyResolverPolicyServiceImpl implements KeyResolverPolicyService {
    private final GatewayKeyResolverPolicyRepository gatewayKeyResolverPolicyRepository;

    @Override
    public KeyResolverPolicyResponse create(CreateKeyResolverPolicyRequest request) {
        if(gatewayKeyResolverPolicyRepository.existsByPolicyCode(request.getPolicyCode())) {
           throw new RuntimeException ("Policy code already exists");
        }
        GatewayKeyResolverPolicy savedKeyResolverPolicy = gatewayKeyResolverPolicyRepository.save(request.toEntity());
        return KeyResolverPolicyResponse.fromEntity(savedKeyResolverPolicy);

    }

}
