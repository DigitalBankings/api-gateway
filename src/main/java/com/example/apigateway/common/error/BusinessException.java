package com.example.apigateway.common.error;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

  private final GatewayErrorCode errorCode;

  // Default message from enum
  public BusinessException(GatewayErrorCode errorCode) {
    super(errorCode.getDefaultMessage());
    this.errorCode = errorCode;
  }

  // Custom message
  public BusinessException(GatewayErrorCode errorCode, String customMessage) {
    super(customMessage);
    this.errorCode = errorCode;
  }
}
