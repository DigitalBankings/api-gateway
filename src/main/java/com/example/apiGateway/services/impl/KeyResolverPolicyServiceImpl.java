package com.example.apiGateway.services.impl;

import com.example.apiGateway.dto.keyResolverPolicy.CreateKeyResolverPolicyRequest;
import com.example.apiGateway.dto.keyResolverPolicy.KeyResolverPolicyResponse;
import com.example.apiGateway.enums.KeyResolverStrategy;
import com.example.apiGateway.modles.GatewayKeyResolverPolicy;
import com.example.apiGateway.repositories.GatewayKeyResolverPolicyRepository;
import com.example.apiGateway.services.KeyResolverPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

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

    validateStrategy(request);

    GatewayKeyResolverPolicy saved = repository.save(request.toEntity());

    return KeyResolverPolicyResponse.fromEntity(saved);
  }

  // ----------------------
  // Helper Methods
  // ----------------------
  private void validateStrategy(CreateKeyResolverPolicyRequest req) {
    if (req.getStrategy() == KeyResolverStrategy.HEADER) {
      if (req.getHeaderName() == null || req.getHeaderName().isBlank()) {
        throw new IllegalArgumentException("headerName is required for HEADER strategy");
      }
    } else {
      if (req.getHeaderName() != null && !req.getHeaderName().isBlank()) {
        throw new IllegalArgumentException("headerName is only allowed for HEADER strategy");
      }
    }
  }

}
