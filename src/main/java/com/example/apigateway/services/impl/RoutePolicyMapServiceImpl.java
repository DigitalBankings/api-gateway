  package com.example.apigateway.services.impl;
  import com.example.apigateway.dto.gatewayroute.GatewayRouteConfigResponse;
  import com.example.apigateway.dto.gatewayroutemapdto.CreateGatewayRouteMapRequest;
  import com.example.apigateway.dto.gatewayroutemapdto.GetRouteMapRequest;
  import com.example.apigateway.dto.gatewayroutemapdto.GetRouteMapResponse;
  import com.example.apigateway.enums.Status;
  import com.example.apigateway.modles.*;
  import com.example.apigateway.repositories.*;
  import com.example.apigateway.services.RoutePolicyMapService;
  import com.example.apigateway.services.caches.GatewayRouteStore;
  import jakarta.persistence.criteria.Join;
  import jakarta.persistence.criteria.Predicate;
  import jakarta.transaction.Transactional;
  import lombok.RequiredArgsConstructor;
  import org.springframework.beans.factory.annotation.Autowired;
  import org.springframework.context.annotation.Lazy;
  import org.springframework.cloud.gateway.support.NotFoundException;
  import org.springframework.data.domain.Page;
  import org.springframework.data.domain.PageRequest;
  import org.springframework.data.domain.Pageable;
  import org.springframework.data.jpa.domain.Specification;
  import org.springframework.stereotype.Service;

  import java.util.*;


  @Service
  @RequiredArgsConstructor
  public class RoutePolicyMapServiceImpl implements RoutePolicyMapService {

    private final GatewayRoutePolicyMapRepository routePolicyMapRepository;
    private final GatewayRouteRepository routeRepository;
    private final GatewayRateLimitPolicyRepository rateLimitRepository;
    private final GatewayCircuityBreakerPolicyRepository circuitBreakerRepository;
    private final GatewayKeyResolverPolicyRepository keyResolverRepository;
    private GatewayRouteStore gatewayRouteStore; // don't use constructor injection

    @Autowired
    public void setGatewayRouteStore(@Lazy GatewayRouteStore gatewayRouteStore) {
      this.gatewayRouteStore = gatewayRouteStore;
    }

    @Override
    @Transactional
    public GatewayRouteConfigResponse createRouteMap(CreateGatewayRouteMapRequest request) {

      GatewayRoute route = routeRepository.findById(request.getRouteId())
              .orElseThrow(() -> new NotFoundException("Route not found"));

      GatewayRateLimitPolicy rateLimit = request.getRateLimitPolicyId() != null
              ? rateLimitRepository.findById(request.getRateLimitPolicyId()).orElse(null)
              : null;

      GatewayKeyResolverPolicy keyResolver = (rateLimit != null && rateLimit.getKeyResolverPolicyId() != null)
              ? keyResolverRepository.findById(rateLimit.getKeyResolverPolicyId()).orElse(null)
              : null;

      GatewayCircuityBreakerPolicy cb = request.getCircuitBreakerPolicyId() != null
              ? circuitBreakerRepository.findById(request.getCircuitBreakerPolicyId()).orElse(null)
              : null;

      GatewayRoutePolicyMap map = GatewayRoutePolicyMap.builder()
              .routeId(route.getId())
              .rateLimitPolicyId(rateLimit != null ? rateLimit.getId() : null)
              .circuitBreakerPolicyId(cb != null ? cb.getId() : null)
              .status(Status.ACTIVE)
              .build();

      routePolicyMapRepository.save(map);

      GatewayRouteConfigResponse response = GatewayRouteConfigResponse.builder()
              .routeId(route.getRouteCode())
              .serviceName(route.getServiceName())
              .uri(route.getTargetUri())
              .predicates(GatewayRouteConfigResponse.Predicates.builder()
                      .path(route.getPath())
                      .method(route.getHttpMethod())
                      .build())
              .filters(GatewayRouteConfigResponse.Filters.builder()
                      .timeoutMs(route.getTimeOutMs())
                      .authRequired(route.getAuthRequired())
                      .rateLimit(rateLimit != null
                              ? GatewayRouteConfigResponse.RateLimit.builder()
                              .replenishRate(rateLimit.getReplenishRate())
                              .burstCapacity(rateLimit.getBurstCapacity())
                              .windowSeconds(rateLimit.getWindowSeconds())
                              .keyResolver(keyResolver != null
                                      ? GatewayRouteConfigResponse.KeyResolver.builder()
                                      .strategy(keyResolver.getStrategy().name())
                                      .headerName(keyResolver.getHeaderName())
                                      .fallbackStrategy(
                                              keyResolver.getFallbackStrategy() != null
                                                      ? keyResolver.getFallbackStrategy().name()
                                                      : null)
                                      .build()
                                      : null)
                              .build()
                              : null)
                      .circuitBreaker(cb != null
                              ? GatewayRouteConfigResponse.CircuitBreaker.builder()
                              .slidingWindowType(cb.getSlidingWindowType().name())
                              .windowSize(cb.getSlidingWindowSize())
                              .failureRateThreshold(cb.getFailureRateThreshold())
                              .slowCallRateThreshold(cb.getSlowCallRateThreshold())
                              .slowCallDurationMs(cb.getSlowCallDurationMs())
                              .openStateWaitMs(cb.getOpenStateWaitMs())
                              .halfOpenCalls(cb.getHalfOpenCalls())
                              .timeoutMs(cb.getTimeoutMs())
                              .build()
                              : null)
                      .build())
              .build();

// ✅ Store in Redis via Spring Cache
      gatewayRouteStore.put(response);

      return response;

    }

    @Override
    @Transactional
    public GetRouteMapResponse getAllRouteMaps(GetRouteMapRequest request) {

      Pageable pageable = PageRequest.of(request.getPage(), request.getSize());

      // 1️⃣ Build Specification for optional filters
      Specification<GatewayRoutePolicyMap> spec = (root, query, cb) -> {
        List<Predicate> predicates = new ArrayList<>();

        // Join route for serviceName filter
        if (request.getServiceName() != null && !request.getServiceName().isBlank()) {
          Join<GatewayRoutePolicyMap, GatewayRoute> routeJoin = root.join("route");
          predicates.add(cb.like(cb.lower(routeJoin.get("serviceName")),
                  "%" + request.getServiceName().toLowerCase() + "%"));
        }

        // Status filter
        if (request.getStatus() != null) {
          predicates.add(cb.equal(root.get("status"), request.getStatus()));
        }

        return cb.and(predicates.toArray(new Predicate[0]));
      };

      // 2️⃣ Query DB with Specification + pagination
      Page<GatewayRoutePolicyMap> pageMap = routePolicyMapRepository.findAll(spec, pageable);

      // 3️⃣ Map each unique route
      Map<Long, GatewayRouteConfigResponse> routeMap = new LinkedHashMap<>();

      for (GatewayRoutePolicyMap map : pageMap.getContent()) {
        GatewayRoute route = routeRepository.findById(map.getRouteId()).orElse(null);
        if (route == null) continue;

        if (!routeMap.containsKey(route.getId())) {
          // Load policies
          GatewayRateLimitPolicy rateLimit = map.getRateLimitPolicyId() != null
                  ? rateLimitRepository.findById(map.getRateLimitPolicyId()).orElse(null)
                  : null;

          GatewayKeyResolverPolicy keyResolver = (rateLimit != null && rateLimit.getKeyResolverPolicyId() != null)
                  ? keyResolverRepository.findById(rateLimit.getKeyResolverPolicyId()).orElse(null)
                  : null;

          GatewayCircuityBreakerPolicy cb = map.getCircuitBreakerPolicyId() != null
                  ? circuitBreakerRepository.findById(map.getCircuitBreakerPolicyId()).orElse(null)
                  : null;

          GatewayRouteConfigResponse response = GatewayRouteConfigResponse.builder()
                  .routeId(route.getRouteCode())
                  .serviceName(route.getServiceName())
                  .uri(route.getTargetUri())
                  .predicates(GatewayRouteConfigResponse.Predicates.builder()
                          .path(route.getPath())
                          .method(route.getHttpMethod())
                          .build())
                  .filters(GatewayRouteConfigResponse.Filters.builder()
                          .timeoutMs(route.getTimeOutMs())
                          .authRequired(route.getAuthRequired())
                          .rateLimit(rateLimit != null ? GatewayRouteConfigResponse.RateLimit.builder()
                                  .replenishRate(rateLimit.getReplenishRate())
                                  .burstCapacity(rateLimit.getBurstCapacity())
                                  .windowSeconds(rateLimit.getWindowSeconds())
                                  .keyResolver(keyResolver != null ? GatewayRouteConfigResponse.KeyResolver.builder()
                                          .strategy(keyResolver.getStrategy().name())
                                          .headerName(keyResolver.getHeaderName())
                                          .fallbackStrategy(keyResolver.getFallbackStrategy() != null
                                                  ? keyResolver.getFallbackStrategy().name()
                                                  : null)
                                          .build() : null)
                                  .build() : null)
                          .circuitBreaker(cb != null ? GatewayRouteConfigResponse.CircuitBreaker.builder()
                                  .slidingWindowType(cb.getSlidingWindowType().name())
                                  .windowSize(cb.getSlidingWindowSize())
                                  .failureRateThreshold(cb.getFailureRateThreshold())
                                  .slowCallRateThreshold(cb.getSlowCallRateThreshold())
                                  .slowCallDurationMs(cb.getSlowCallDurationMs())
                                  .openStateWaitMs(cb.getOpenStateWaitMs())
                                  .halfOpenCalls(cb.getHalfOpenCalls())
                                  .timeoutMs(cb.getTimeoutMs())
                                  .build() : null)
                          .build())
                  .build();

          routeMap.put(route.getId(), response);
        }
      }

      // 4️⃣ Build final response
      GetRouteMapResponse response = new GetRouteMapResponse();
      response.setTotalElements(pageMap.getTotalElements());
      response.setTotalPages(pageMap.getTotalPages());
      response.setCurrentPage(pageMap.getNumber());
      response.setItems(new ArrayList<>(routeMap.values())); // unique routes only

      return response;
    }

    @Override
    @Transactional
    public GatewayRouteConfigResponse getRouteConfigFromDB(String routeCode) {

      GatewayRoute route = routeRepository.findByRouteCode(routeCode)
              .orElseThrow(() -> new NotFoundException("Route not found"));

      GatewayRoutePolicyMap map = routePolicyMapRepository
              .findFirstByRouteIdAndStatus(route.getId(), Status.ACTIVE)
              .orElseThrow(() -> new NotFoundException("Route policy not found"));

      GatewayRateLimitPolicy rateLimit = map.getRateLimitPolicyId() != null
              ? rateLimitRepository.findById(map.getRateLimitPolicyId()).orElse(null)
              : null;

      GatewayKeyResolverPolicy keyResolver = (rateLimit != null && rateLimit.getKeyResolverPolicyId() != null)
              ? keyResolverRepository.findById(rateLimit.getKeyResolverPolicyId()).orElse(null)
              : null;

      GatewayCircuityBreakerPolicy cb = map.getCircuitBreakerPolicyId() != null
              ? circuitBreakerRepository.findById(map.getCircuitBreakerPolicyId()).orElse(null)
              : null;

      return GatewayRouteConfigResponse.builder()
              .routeId(route.getRouteCode())
              .serviceName(route.getServiceName())
              .uri(route.getTargetUri())
              .predicates(GatewayRouteConfigResponse.Predicates.builder()
                      .path(route.getPath())
                      .method(route.getHttpMethod())
                      .build())
              .filters(GatewayRouteConfigResponse.Filters.builder()
                      .timeoutMs(route.getTimeOutMs())
                      .authRequired(route.getAuthRequired())
                      .rateLimit(rateLimit != null ? GatewayRouteConfigResponse.RateLimit.builder()
                              .replenishRate(rateLimit.getReplenishRate())
                              .burstCapacity(rateLimit.getBurstCapacity())
                              .windowSeconds(rateLimit.getWindowSeconds())
                              .keyResolver(keyResolver != null ? GatewayRouteConfigResponse.KeyResolver.builder()
                                      .strategy(keyResolver.getStrategy().name())
                                      .headerName(keyResolver.getHeaderName())
                                      .fallbackStrategy(keyResolver.getFallbackStrategy() != null
                                              ? keyResolver.getFallbackStrategy().name()
                                              : null)
                                      .build() : null)
                              .build() : null)
                      .circuitBreaker(cb != null ? GatewayRouteConfigResponse.CircuitBreaker.builder()
                              .slidingWindowType(cb.getSlidingWindowType().name())
                              .windowSize(cb.getSlidingWindowSize())
                              .failureRateThreshold(cb.getFailureRateThreshold())
                              .slowCallRateThreshold(cb.getSlowCallRateThreshold())
                              .slowCallDurationMs(cb.getSlowCallDurationMs())
                              .openStateWaitMs(cb.getOpenStateWaitMs())
                              .halfOpenCalls(cb.getHalfOpenCalls())
                              .timeoutMs(cb.getTimeoutMs())
                              .build() : null)
                      .build())
              .build();
    }

    @Override
    @Transactional
    public Collection<GatewayRouteConfigResponse> getAllRoutesFromDB() {
      GetRouteMapRequest request = new GetRouteMapRequest();
      request.setPage(0);
      request.setSize(Integer.MAX_VALUE); // get all routes
      GetRouteMapResponse response = getAllRouteMaps(request);
      return response.getItems();
    }

  }
