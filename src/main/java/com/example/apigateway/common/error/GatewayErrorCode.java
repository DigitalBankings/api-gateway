package com.example.apigateway.common.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum GatewayErrorCode {
  SERVICE_TIMEOUT("EGW504", HttpStatus.GATEWAY_TIMEOUT, "Service response exceeded timeout limit"),

  SERVICE_UNAVAILABLE("EGW503", HttpStatus.SERVICE_UNAVAILABLE, "Service temporarily unavailable"),

  ROUTE_NOT_FOUND("E0001", HttpStatus.NOT_FOUND, "GatewayRoute not found"),

  CIRCUIT_OPEN("EGW503CB", HttpStatus.SERVICE_UNAVAILABLE, "Service unavailable due to high load"),

  BAD_REQUEST("EGW400", HttpStatus.BAD_REQUEST, "Invalid request"),

  VALIDATION_ERROR("EGW422", HttpStatus.UNPROCESSABLE_ENTITY, "Validation failed"),

  INTERNAL_ERROR("EGW500", HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected gateway error");

  private final String code;
  private final HttpStatus status;
  private final String defaultMessage;
}
