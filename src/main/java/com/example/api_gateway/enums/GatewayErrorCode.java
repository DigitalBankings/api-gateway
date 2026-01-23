package com.example.api_gateway.enums;

public enum GatewayErrorCode {
  GATEWAY_TIMEOUT,
  UPSTREAM_SERVICE_DOWN,
  CIRCUIT_BREAKER_OPEN,
  INVALID_REQUEST,
  BUSINESS_ERROR,
  INTERNAL_GATEWAY_ERROR
}
