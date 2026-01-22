package com.example.apiGateway.controller;

import com.example.apiGateway.dto.rateLimitPolicy.CreateRateLimitPolicy;
import com.example.apiGateway.dto.rateLimitPolicy.RateLimitPolicyResponse;
import com.example.apiGateway.services.RateLimitPolicyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
