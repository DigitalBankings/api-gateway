package com.example.apigateway.config;

import java.time.Duration;
import java.util.concurrent.TimeoutException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

@Component("CustomTimeout") // matches filter name in DynamicRouteLocator
public class CustomTimeoutGatewayFilterFactory
    extends AbstractGatewayFilterFactory<CustomTimeoutGatewayFilterFactory.Config> {

  public CustomTimeoutGatewayFilterFactory() {
    super(Config.class);
  }

  @Override
  public GatewayFilter apply(Config config) {
    return (exchange, chain) ->
        chain
            .filter(exchange)
            .timeout(Duration.ofMillis(config.getTimeoutMs()))
            .onErrorMap(
                throwable ->
                    new TimeoutException("Request exceeded " + config.getTimeoutMs() + "ms"));
  }

  @Getter
  @Setter
  public static class Config {
    private long timeoutMs; // timeout in milliseconds
  }
}
