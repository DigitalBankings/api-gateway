package com.example.apigateway.services;

import com.example.apigateway.dto.keyresolverpolicy.CreateKeyResolverPolicyRequest;
import com.example.apigateway.dto.keyresolverpolicy.KeyResolverPolicyResponse;

public interface KeyResolverPolicyService {
  KeyResolverPolicyResponse create(CreateKeyResolverPolicyRequest request);
}
