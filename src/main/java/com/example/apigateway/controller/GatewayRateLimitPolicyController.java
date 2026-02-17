package com.example.apigateway.controller;

import com.example.apigateway.dto.gatewayroute.PagedResponse;
import com.example.apigateway.dto.ratelimitpolicy.CreateRateLimitPolicy;
import com.example.apigateway.dto.ratelimitpolicy.ListRateLimitPolicyRequest;
import com.example.apigateway.dto.ratelimitpolicy.RateLimitPolicyResponse;
import com.example.apigateway.services.RateLimitPolicyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("v1/rate-limit/")
@RequiredArgsConstructor
public class GatewayRateLimitPolicyController {

  private final RateLimitPolicyService rateLimitPolicyService;

  @PostMapping("register")
  public RateLimitPolicyResponse createRateLimitPolicy(
      @RequestBody CreateRateLimitPolicy createRateLimitPolicy) {
    log.info("createRateLimitPolicy : {}", createRateLimitPolicy);
    return rateLimitPolicyService.create(createRateLimitPolicy);
  }

  @GetMapping("getOne/{id}")
  public RateLimitPolicyResponse getOne(@PathVariable Long id) {
    return rateLimitPolicyService.getOne(id);
  }

  @PostMapping("getAll")
  public PagedResponse<RateLimitPolicyResponse> getAll(
      @RequestBody ListRateLimitPolicyRequest request) {
    return rateLimitPolicyService.getAll(request);
  }
}
