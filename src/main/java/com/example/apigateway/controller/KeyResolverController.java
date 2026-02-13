package com.example.apigateway.controller;

import com.example.apigateway.dto.keyresolverpolicy.CreateKeyResolverPolicyRequest;
import com.example.apigateway.dto.keyresolverpolicy.KeyResolverPolicyResponse;
import com.example.apigateway.services.KeyResolverPolicyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/key-resolvers")
@RequiredArgsConstructor
public class KeyResolverController {

  private final KeyResolverPolicyService keyResolverPolicyService;

  @PostMapping("register")
  public KeyResolverPolicyResponse create(@RequestBody CreateKeyResolverPolicyRequest request) {
    KeyResolverPolicyResponse response = keyResolverPolicyService.create(request);
    return response;
  }
}
