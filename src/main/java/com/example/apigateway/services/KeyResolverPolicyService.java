package com.example.apigateway.services;

import com.example.apigateway.dto.gatewayroute.PagedResponse;
import com.example.apigateway.dto.keyresolverpolicy.CreateKeyResolverPolicyRequest;
import com.example.apigateway.dto.keyresolverpolicy.KeyResolverPolicyResponse;
import com.example.apigateway.dto.keyresolverpolicy.ListKeyResolverPolicyRequest;
import com.example.apigateway.dto.keyresolverpolicy.UpdateKeyResolverPolicyRequest;

public interface KeyResolverPolicyService {
  KeyResolverPolicyResponse create(CreateKeyResolverPolicyRequest request);

  KeyResolverPolicyResponse getOne(Long id);

  PagedResponse<KeyResolverPolicyResponse> getAll(ListKeyResolverPolicyRequest request);

  KeyResolverPolicyResponse updateById(UpdateKeyResolverPolicyRequest request);
}
