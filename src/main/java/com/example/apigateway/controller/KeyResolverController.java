package com.example.apigateway.controller;

import com.example.apigateway.dto.gatewayroute.PagedResponse;
import com.example.apigateway.dto.keyresolverpolicy.CreateKeyResolverPolicyRequest;
import com.example.apigateway.dto.keyresolverpolicy.KeyResolverPolicyResponse;
import com.example.apigateway.dto.keyresolverpolicy.ListKeyResolverPolicyRequest;
import com.example.apigateway.dto.keyresolverpolicy.UpdateKeyResolverPolicyRequest;
import com.example.apigateway.services.KeyResolverPolicyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/key-resolvers")
@RequiredArgsConstructor
public class KeyResolverController {

  private final KeyResolverPolicyService keyResolverPolicyService;

  @PostMapping("register")
  public KeyResolverPolicyResponse create(@RequestBody CreateKeyResolverPolicyRequest request) {
    return keyResolverPolicyService.create(request);
  }

  @GetMapping("getOne/{id}")
  public KeyResolverPolicyResponse getOne(@PathVariable Long id) {
    return keyResolverPolicyService.getOne(id);
  }

  @PostMapping("getAll")
  public PagedResponse<KeyResolverPolicyResponse> getAll(
      @RequestBody ListKeyResolverPolicyRequest request) {
    return keyResolverPolicyService.getAll(request);
  }

  @PostMapping("updateById")
  public KeyResolverPolicyResponse updateById(@RequestBody UpdateKeyResolverPolicyRequest request) {
    return keyResolverPolicyService.updateById(request);
  }
}
