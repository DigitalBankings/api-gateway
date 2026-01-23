package com.example.apigateway.exceptionHandle;

import com.example.apigateway.enums.GatewayErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {

  private final GatewayErrorCode errorCode;
  private final HttpStatus status;

  public BusinessException(GatewayErrorCode errorCode, String message, HttpStatus status) {
    super(message);
    this.errorCode = errorCode;
    this.status = status;
  }

  /* Optional convenience factory */
  public static BusinessException conflict(GatewayErrorCode code, String message) {
    return new BusinessException(code, message, HttpStatus.CONFLICT);
  }

  public static BusinessException badRequest(GatewayErrorCode code, String message) {
    return new BusinessException(code, message, HttpStatus.BAD_REQUEST);
  }
}
