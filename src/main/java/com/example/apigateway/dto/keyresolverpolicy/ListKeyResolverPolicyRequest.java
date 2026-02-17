package com.example.apigateway.dto.keyresolverpolicy;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ListKeyResolverPolicyRequest {
  private int page;
  private int size;
}
