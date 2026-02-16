package com.example.apigateway.common.error;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class GatewayErrorResponse {

  private String errorCode;
  private String message;
  private int status;
  private String path;
  private String method;
  private String traceId;
  private LocalDateTime timestamp;
}
