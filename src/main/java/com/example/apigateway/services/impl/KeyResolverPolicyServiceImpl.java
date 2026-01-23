package com.example.apigateway.services.impl;

import com.example.apigateway.dto.keyresolverpolicy.CreateKeyResolverPolicyRequest;
import com.example.apigateway.dto.keyresolverpolicy.KeyResolverPolicyResponse;
import com.example.apigateway.modles.GatewayKeyResolverPolicy;
import com.example.apigateway.repositories.GatewayKeyResolverPolicyRepository;
import com.example.apigateway.services.KeyResolverPolicyService;
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
