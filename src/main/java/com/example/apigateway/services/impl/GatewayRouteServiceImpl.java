package com.example.apigateway.services.impl;

import com.example.apigateway.common.error.BusinessException;
import com.example.apigateway.common.error.GatewayErrorCode;
import com.example.apigateway.dto.gatewayroute.*;
import com.example.apigateway.modles.GatewayRoute;
import com.example.apigateway.repositories.GatewayRouteRepository;
import com.example.apigateway.services.GatewayRouteService;
import com.example.apigateway.services.caches.GatewayRouteStore;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GatewayRouteServiceImpl implements GatewayRouteService {

  private final GatewayRouteRepository gatewayRouteRepository;
  private final GatewayRouteStore gatewayRouteStore;

  @Override
  public GatewayRouteResponse create(CreateGatewayRouteRequest request) {
    GatewayRoute save = gatewayRouteRepository.save(request.toEntity());
    return GatewayRouteResponse.fromEntity(save);
  }

  @Override
  public PagedResponse<GatewayRouteResponse> getAll(GetAllRequest request) {
    Pageable pageable =
        PageRequest.of(
            request.getPage() - 1, request.getSize(), Sort.by(Sort.Direction.DESC, "id"));
    Page<GatewayRoute> pageResult = gatewayRouteRepository.findAll(pageable);
    List<GatewayRouteResponse> data =
        pageResult.getContent().stream().map(GatewayRouteResponse::fromEntity).toList();
    PagedResponse.Pagination pagination =
        new PagedResponse.Pagination(
            pageResult.getSize(),
            pageResult.getTotalPages(),
            pageResult.getTotalElements(),
            pageResult.getNumber() + 1);
    return new PagedResponse<>(data, pagination);
  }

  @Override
  public GatewayRouteResponse getOneById(Long routeId) {
    GatewayRoute route = gatewayRouteRepository.findById(routeId).orElse(null);
    if (route == null) {
      throw new BusinessException(
          GatewayErrorCode.ROUTE_NOT_FOUND, "GatewayRoute not found with route id: " + routeId);
    }
    return GatewayRouteResponse.fromEntity(route);
  }

  @Override
  public GatewayRouteResponse updateRouteById(UpdateGatewayRouteRequest request) {

    GatewayRoute route = gatewayRouteRepository.findById(request.getRouteId()).orElse(null);
    if (route == null) {
      throw new BusinessException(
          GatewayErrorCode.ROUTE_NOT_FOUND,
          "GatewayRoute not found with route id: " + request.getRouteId());
    }

    request.updateEntity(route);
    GatewayRoute updated = gatewayRouteRepository.save(route);
    return GatewayRouteResponse.fromEntity(updated);
  }
}
