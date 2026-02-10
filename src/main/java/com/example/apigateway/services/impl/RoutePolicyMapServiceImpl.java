package com.example.apigateway.services.impl;

import com.example.apigateway.dto.gatewayroute.GatewayRouteConfigResponse;
import com.example.apigateway.dto.gatewayroutemapdto.*;
import com.example.apigateway.enums.Status;
import com.example.apigateway.mapper.GatewayRouteConfigMapper;
import com.example.apigateway.modles.*;
import com.example.apigateway.repositories.*;
import com.example.apigateway.resolver.RoutePolicyResolver;
import com.example.apigateway.services.RoutePolicyMapService;
import com.example.apigateway.services.caches.GatewayRouteStore;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoutePolicyMapServiceImpl implements RoutePolicyMapService {

  private final GatewayRoutePolicyMapRepository routePolicyMapRepository;
  private final GatewayRouteRepository routeRepository;
  private final GatewayRateLimitPolicyRepository rateLimitRepository;
  private final GatewayCircuityBreakerPolicyRepository circuitBreakerRepository;
  private final GatewayKeyResolverPolicyRepository keyResolverRepository;
  private final GatewayRouteConfigMapper mapper;
  private final RoutePolicyResolver policyResolver;

  private GatewayRouteStore gatewayRouteStore;

  @Autowired
  public void setGatewayRouteStore(@Lazy GatewayRouteStore gatewayRouteStore) {
    this.gatewayRouteStore = gatewayRouteStore;
  }

  // ================= CREATE =================
  @Override
  @Transactional
  public GatewayRouteConfigResponse createRouteMap(CreateGatewayRouteMapRequest request) {

    GatewayRoute route =
        routeRepository
            .findById(request.getRouteId())
            .orElseThrow(() -> new NotFoundException("Route not found"));

    GatewayRoutePolicyMap map =
        GatewayRoutePolicyMap.builder()
            .routeId(route.getId())
            .rateLimitPolicyId(request.getRateLimitPolicyId())
            .circuitBreakerPolicyId(request.getCircuitBreakerPolicyId())
            .status(Status.ACTIVE)
            .build();

    routePolicyMapRepository.save(map);

    RoutePolicyResolver.ResolvedPolicies policies = policyResolver.resolve(map);

    GatewayRouteConfigResponse response =
        mapper.toResponse(
            route, policies.rateLimit(), policies.keyResolver(), policies.circuitBreaker());

    gatewayRouteStore.put(response);
    gatewayRouteStore.refreshAllRoutes();

    return response;
  }

  // ================= GET PAGED =================
  @Override
  @Transactional(Transactional.TxType.SUPPORTS)
  public GetRouteMapResponse getAllRouteMaps(GetRouteMapRequest request) {

    Pageable pageable = PageRequest.of(request.getPage(), request.getSize());

    Specification<GatewayRoutePolicyMap> spec =
        (root, query, cb) -> {
          List<Predicate> predicates = new ArrayList<>();

          if (request.getServiceName() != null && !request.getServiceName().isBlank()) {
            Join<GatewayRoutePolicyMap, GatewayRoute> routeJoin = root.join("route");
            predicates.add(
                cb.like(
                    cb.lower(routeJoin.get("serviceName")),
                    "%" + request.getServiceName().toLowerCase() + "%"));
          }

          if (request.getStatus() != null) {
            predicates.add(cb.equal(root.get("status"), request.getStatus()));
          }

          return cb.and(predicates.toArray(new Predicate[0]));
        };

    Page<GatewayRoutePolicyMap> pageMap = routePolicyMapRepository.findAll(spec, pageable);
    List<GatewayRoutePolicyMap> maps = pageMap.getContent();

    // ===== BULK FETCH =====
    Map<Long, GatewayRoute> routes =
        routeRepository
            .findAllById(
                maps.stream().map(GatewayRoutePolicyMap::getRouteId).collect(Collectors.toSet()))
            .stream()
            .collect(Collectors.toMap(GatewayRoute::getId, Function.identity()));

    Map<Long, GatewayRateLimitPolicy> rateLimits =
        rateLimitRepository
            .findAllById(
                maps.stream()
                    .map(GatewayRoutePolicyMap::getRateLimitPolicyId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet()))
            .stream()
            .collect(Collectors.toMap(GatewayRateLimitPolicy::getId, Function.identity()));

    Map<Long, GatewayCircuityBreakerPolicy> circuitBreakers =
        circuitBreakerRepository
            .findAllById(
                maps.stream()
                    .map(GatewayRoutePolicyMap::getCircuitBreakerPolicyId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet()))
            .stream()
            .collect(Collectors.toMap(GatewayCircuityBreakerPolicy::getId, Function.identity()));

    Map<Long, GatewayKeyResolverPolicy> keyResolvers =
        keyResolverRepository
            .findAllById(
                rateLimits.values().stream()
                    .map(GatewayRateLimitPolicy::getKeyResolverPolicyId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet()))
            .stream()
            .collect(Collectors.toMap(GatewayKeyResolverPolicy::getId, Function.identity()));

    Map<Long, GatewayRouteConfigResponse> routeMap = new LinkedHashMap<>();

    for (GatewayRoutePolicyMap map : maps) {
      GatewayRoute route = routes.get(map.getRouteId());
      if (route == null) continue;

      GatewayRateLimitPolicy rateLimit = rateLimits.get(map.getRateLimitPolicyId());
      GatewayKeyResolverPolicy keyResolver =
          rateLimit == null ? null : keyResolvers.get(rateLimit.getKeyResolverPolicyId());
      GatewayCircuityBreakerPolicy cb = circuitBreakers.get(map.getCircuitBreakerPolicyId());

      routeMap.putIfAbsent(route.getId(), mapper.toResponse(route, rateLimit, keyResolver, cb));
    }

    GetRouteMapResponse response = new GetRouteMapResponse();
    response.setTotalElements(pageMap.getTotalElements());
    response.setTotalPages(pageMap.getTotalPages());
    response.setCurrentPage(pageMap.getNumber());
    response.setItems(new ArrayList<>(routeMap.values()));

    return response;
  }

  // ================= GET SINGLE =================
  @Override
  @Transactional(Transactional.TxType.SUPPORTS)
  public GatewayRouteConfigResponse getRouteConfigFromDB(String routeCode) {

    GatewayRoute route =
        routeRepository
            .findByRouteCode(routeCode)
            .orElseThrow(() -> new NotFoundException("Route not found"));

    GatewayRoutePolicyMap map =
        routePolicyMapRepository
            .findFirstByRouteIdAndStatus(route.getId(), Status.ACTIVE)
            .orElseThrow(() -> new NotFoundException("Route policy not found"));

    RoutePolicyResolver.ResolvedPolicies policies = policyResolver.resolve(map);
    return mapper.toResponse(
        route, policies.rateLimit(), policies.keyResolver(), policies.circuitBreaker());
  }

  // ================= SAFE GET ALL =================
  @Override
  @Transactional(Transactional.TxType.SUPPORTS)
  public Collection<GatewayRouteConfigResponse> getAllRoutesFromDB() {
    int page = 0;
    int size = 500;
    List<GatewayRouteConfigResponse> all = new ArrayList<>();
    Page<GatewayRoutePolicyMap> result;
    do {
      result = routePolicyMapRepository.findAll(PageRequest.of(page++, size));
      GetRouteMapRequest req = new GetRouteMapRequest();
      req.setPage(result.getNumber());
      req.setSize(size);
      all.addAll(getAllRouteMaps(req).getItems());
    } while (result.hasNext());
    return all;
  }
}
