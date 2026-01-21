package com.example.apiGateway.services;

import com.example.apiGateway.dto.keyResolverPolicy.CreateKeyResolverPolicyRequest;
import com.example.apiGateway.dto.keyResolverPolicy.KeyResolverPolicyResponse;

public interface KeyResolverPolicyService {

  KeyResolverPolicyResponse create(CreateKeyResolverPolicyRequest request);
}
