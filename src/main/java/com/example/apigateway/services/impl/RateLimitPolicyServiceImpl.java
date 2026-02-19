package com.example.apigateway.services.impl;

import com.example.apigateway.common.error.BusinessException;
import com.example.apigateway.common.error.GatewayErrorCode;
import com.example.apigateway.dto.gatewayroute.PagedResponse;
import com.example.apigateway.dto.ratelimitpolicy.CreateRateLimitPolicy;
import com.example.apigateway.dto.ratelimitpolicy.ListRateLimitPolicyRequest;
import com.example.apigateway.dto.ratelimitpolicy.RateLimitPolicyResponse;
import com.example.apigateway.dto.ratelimitpolicy.UpdateRateLimitRequest;
import com.example.apigateway.modles.GatewayRateLimitPolicy;
import com.example.apigateway.repositories.GatewayRateLimitPolicyRepository;
import com.example.apigateway.services.RateLimitPolicyService;
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
public class RateLimitPolicyServiceImpl implements RateLimitPolicyService {

  private final GatewayRateLimitPolicyRepository rateLimitPolicyRepository;

  @Override
  public RateLimitPolicyResponse create(CreateRateLimitPolicy createRateLimitPolicy) {
    GatewayRateLimitPolicy gatewayRateLimitPolicy =
        rateLimitPolicyRepository.save(createRateLimitPolicy.toEntity());
    return RateLimitPolicyResponse.fromEntity(gatewayRateLimitPolicy);
  }

  @Override
  public RateLimitPolicyResponse getOne(Long id) {
    GatewayRateLimitPolicy gatewayRateLimitPolicy =
        rateLimitPolicyRepository.findById(id).orElse(null);
    if (gatewayRateLimitPolicy == null) {
      throw new BusinessException(
          GatewayErrorCode.RATE_LIMIT_NOT_FOUND, "Rate limit policy not found");
    }
    return RateLimitPolicyResponse.fromEntity(gatewayRateLimitPolicy);
  }

  @Override
  public PagedResponse<RateLimitPolicyResponse> getAll(ListRateLimitPolicyRequest request) {
    Pageable pageable =
        PageRequest.of(
            request.getPage() - 1, request.getSize(), Sort.by(Sort.Direction.DESC, "id"));
    Page<GatewayRateLimitPolicy> pageResult = rateLimitPolicyRepository.findAll(pageable);
    List<RateLimitPolicyResponse> data =
        pageResult.getContent().stream().map(RateLimitPolicyResponse::fromEntity).toList();
    PagedResponse.Pagination pagination =
        new PagedResponse.Pagination(
            pageResult.getSize(),
            pageResult.getTotalPages(),
            pageResult.getTotalElements(),
            pageResult.getNumber() + 1);
    return new PagedResponse<>(data, pagination);
  }

  @Override
  public RateLimitPolicyResponse updateById(UpdateRateLimitRequest request) {
    GatewayRateLimitPolicy gatewayRateLimitPolicy =
        rateLimitPolicyRepository.findById(request.getId()).orElse(null);
    if (gatewayRateLimitPolicy == null) {
      throw new BusinessException(
          GatewayErrorCode.RATE_LIMIT_NOT_FOUND, "Rate limit policy not found");
    }
    request.updateEntity(gatewayRateLimitPolicy);
    GatewayRateLimitPolicy update = rateLimitPolicyRepository.save(gatewayRateLimitPolicy);
    return RateLimitPolicyResponse.fromEntity(update);
  }
}
