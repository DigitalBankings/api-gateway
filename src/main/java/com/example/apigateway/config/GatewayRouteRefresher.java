package com.example.apigateway.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class GatewayRouteRefresher {

  private final RouteDefinitionWriter routeDefinitionWriter;
  private final DynamicRouteLocator dynamicRouteLocator;
  private final ApplicationEventPublisher publisher;

  public Mono<Void> refreshRoutes() {
    return dynamicRouteLocator
        .getRouteDefinitions()
        .flatMap(
            route ->
                routeDefinitionWriter
                    .delete(Mono.just(route.getId()))
                    .onErrorResume(e -> Mono.empty())
                    .then(routeDefinitionWriter.save(Mono.just(route))))
        .then()
        .doOnSuccess(v -> publisher.publishEvent(new RefreshRoutesEvent(this)));
  }

  @PostConstruct
  public void init() {
    refreshRoutes().subscribe(); // push all DB/Redis routes at startup
  }
}
