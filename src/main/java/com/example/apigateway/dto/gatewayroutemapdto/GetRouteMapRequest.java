package com.example.apigateway.dto.gatewayroutemapdto;

import com.example.apigateway.enums.Status;
import lombok.Data;

@Data
public class GetRouteMapRequest {
  private int page = 0;
  private int size = 10;
  private String serviceName; // optional filter
  private Status status; // optional filter
}
