package com.example.api_gateway.services;

import com.example.api_gateway.dto.keyResolverPolicy.CreateKeyResolverPolicyRequest;
import com.example.api_gateway.dto.keyResolverPolicy.KeyResolverPolicyResponse;

public interface KeyResolverPolicyService {

  KeyResolverPolicyResponse create(CreateKeyResolverPolicyRequest request);
}
