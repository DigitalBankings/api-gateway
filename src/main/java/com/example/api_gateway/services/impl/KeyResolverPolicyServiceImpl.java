package com.example.api_gateway.services.impl;

import com.example.api_gateway.dto.keyResolverPolicy.CreateKeyResolverPolicyRequest;
import com.example.api_gateway.dto.keyResolverPolicy.KeyResolverPolicyResponse;
import com.example.api_gateway.modles.GatewayKeyResolverPolicy;
import com.example.api_gateway.repositories.GatewayKeyResolverPolicyRepository;
import com.example.api_gateway.services.KeyResolverPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class KeyResolverPolicyServiceImpl implements KeyResolverPolicyService {

  private final GatewayKeyResolverPolicyRepository repository;

  @Override
  @Transactional
  public KeyResolverPolicyResponse create(CreateKeyResolverPolicyRequest request) {

    if (repository.existsByPolicyCode(request.getPolicyCode())) {
      throw new IllegalArgumentException("Policy code already exists: " + request.getPolicyCode());
    }

    GatewayKeyResolverPolicy saved = repository.save(request.toEntity());

    return KeyResolverPolicyResponse.fromEntity(saved);
  }



}
