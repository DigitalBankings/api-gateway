package com.example.apigateway.config;

public final class CacheNames {
  // Private constructor hides the default public one
  private CacheNames() {
    throw new UnsupportedOperationException("Cannot instantiate this class");
  }
  public static final String GATEWAY_ROUTE = "gatewayRoute";
}
