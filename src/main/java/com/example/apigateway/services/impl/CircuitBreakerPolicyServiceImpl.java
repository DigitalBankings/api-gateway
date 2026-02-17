package com.example.apigateway.services.impl;

import com.example.apigateway.common.error.BusinessException;
import com.example.apigateway.common.error.GatewayErrorCode;
import com.example.apigateway.dto.circuitbreaker.CreateCircuitBreaker;
import com.example.apigateway.dto.circuitbreaker.ListAllCircuitBreakerPolicy;
import com.example.apigateway.dto.circuitbreaker.ResponseCircuitBreaker;
import com.example.apigateway.dto.circuitbreaker.UpdateCircuitBreakerRequest;
import com.example.apigateway.dto.gatewayroute.PagedResponse;
import com.example.apigateway.modles.GatewayCircuityBreakerPolicy;
import com.example.apigateway.repositories.GatewayCircuityBreakerPolicyRepository;
import com.example.apigateway.services.CircuitBreakerPolicyService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CircuitBreakerPolicyServiceImpl implements CircuitBreakerPolicyService {
  private final GatewayCircuityBreakerPolicyRepository gatewayCircuityBreakerPolicyRepository;

  @Override
  public ResponseCircuitBreaker create(CreateCircuitBreaker createCircuitBreaker) {
    GatewayCircuityBreakerPolicy saved =
        gatewayCircuityBreakerPolicyRepository.save(createCircuitBreaker.toEntity());
    return ResponseCircuitBreaker.fromEntity(saved);
  }

  @Override
  public PagedResponse<ResponseCircuitBreaker> getAll(ListAllCircuitBreakerPolicy request) {
    Pageable pageable =
        PageRequest.of(
            request.getPage() - 1, request.getSize(), Sort.by(Sort.Direction.DESC, "id"));
    Page<GatewayCircuityBreakerPolicy> pageResult =
        gatewayCircuityBreakerPolicyRepository.findAll(pageable);
    List<ResponseCircuitBreaker> data =
        pageResult.getContent().stream().map(ResponseCircuitBreaker::fromEntity).toList();
    PagedResponse.Pagination pagination =
        new PagedResponse.Pagination(
            pageResult.getSize(),
            pageResult.getTotalPages(),
            pageResult.getTotalElements(),
            pageResult.getNumber() + 1);
    return new PagedResponse<>(data, pagination);
  }

  @Override
  public ResponseCircuitBreaker getOne(Long id) {
    GatewayCircuityBreakerPolicy gatewayCircuityBreakerPolicy =
        gatewayCircuityBreakerPolicyRepository.findById(id).orElse(null);
    if (gatewayCircuityBreakerPolicy == null) {
      throw new BusinessException(GatewayErrorCode.CIRCUIT_NOT_FOUND, "Circuit not found");
    }
    return ResponseCircuitBreaker.fromEntity(gatewayCircuityBreakerPolicy);
  }

  @Override
  public ResponseCircuitBreaker updateById(UpdateCircuitBreakerRequest request) {
    GatewayCircuityBreakerPolicy gatewayCircuityBreakerPolicy =
        gatewayCircuityBreakerPolicyRepository.findById(request.getId()).orElse(null);
    if (gatewayCircuityBreakerPolicy == null) {
      throw new BusinessException(GatewayErrorCode.CIRCUIT_NOT_FOUND, "Circuit not found");
    }
    request.updateEntity(gatewayCircuityBreakerPolicy);
    GatewayCircuityBreakerPolicy updated =
        gatewayCircuityBreakerPolicyRepository.save(gatewayCircuityBreakerPolicy);
    return ResponseCircuitBreaker.fromEntity(updated);
  }
}
