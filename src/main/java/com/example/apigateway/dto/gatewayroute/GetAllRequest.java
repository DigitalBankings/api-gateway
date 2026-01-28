package com.example.apigateway.dto.gatewayroute;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GetAllRequest {
  private int page;
  private int size;
}
