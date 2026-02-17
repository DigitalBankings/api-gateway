package com.example.apigateway.dto.ratelimitpolicy;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ListRateLimitPolicyRequest {
  private int page;
  private int size;
}
