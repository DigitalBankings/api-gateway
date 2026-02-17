package com.example.apigateway.services.impl;

import com.example.apigateway.common.error.BusinessException;
import com.example.apigateway.common.error.GatewayErrorCode;
import com.example.apigateway.dto.gatewayroute.PagedResponse;
import com.example.apigateway.dto.keyresolverpolicy.CreateKeyResolverPolicyRequest;
import com.example.apigateway.dto.keyresolverpolicy.KeyResolverPolicyResponse;
import com.example.apigateway.dto.keyresolverpolicy.ListKeyResolverPolicyRequest;
import com.example.apigateway.dto.keyresolverpolicy.UpdateKeyResolverPolicyRequest;
import com.example.apigateway.modles.GatewayKeyResolverPolicy;
import com.example.apigateway.repositories.GatewayKeyResolverPolicyRepository;
import com.example.apigateway.services.KeyResolverPolicyService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeyResolverPolicyServiceImpl implements KeyResolverPolicyService {

  private final GatewayKeyResolverPolicyRepository keyResolverPolicyRepository;

  @Override
  @Transactional
  public KeyResolverPolicyResponse create(CreateKeyResolverPolicyRequest request) {

    if (keyResolverPolicyRepository.existsByPolicyCode(request.getPolicyCode())) {
      throw new IllegalArgumentException("Policy code already exists: " + request.getPolicyCode());
    }
    GatewayKeyResolverPolicy saved = keyResolverPolicyRepository.save(request.toEntity());
    return KeyResolverPolicyResponse.fromEntity(saved);
  }

  @Override
  public KeyResolverPolicyResponse getOne(Long id) {
    GatewayKeyResolverPolicy gatewayKeyResolverPolicy =
        keyResolverPolicyRepository.findById(id).orElse(null);
    if (gatewayKeyResolverPolicy == null) {
      throw new IllegalArgumentException("Key not found: " + id);
    }
    return KeyResolverPolicyResponse.fromEntity(gatewayKeyResolverPolicy);
  }

  @Override
  public PagedResponse<KeyResolverPolicyResponse> getAll(ListKeyResolverPolicyRequest request) {
    Pageable pageable =
        PageRequest.of(
            request.getPage() - 1, request.getSize(), Sort.by(Sort.Direction.DESC, "id"));
    Page<GatewayKeyResolverPolicy> pageResult = keyResolverPolicyRepository.findAll(pageable);
    List<KeyResolverPolicyResponse> data =
        pageResult.getContent().stream().map(KeyResolverPolicyResponse::fromEntity).toList();
    PagedResponse.Pagination pagination =
        new PagedResponse.Pagination(
            pageResult.getSize(),
            pageResult.getTotalPages(),
            pageResult.getTotalElements(),
            pageResult.getNumber() + 1);
    return new PagedResponse<>(data, pagination);
  }

  @Override
  public KeyResolverPolicyResponse updateById(UpdateKeyResolverPolicyRequest request) {
    GatewayKeyResolverPolicy gatewayKeyResolver =
        keyResolverPolicyRepository.findById(request.getId()).orElse(null);
    if (gatewayKeyResolver == null) {
      throw new BusinessException(
          GatewayErrorCode.ROUTE_NOT_FOUND, "Key resolve not found id :" + request.getId());
    }
    request.updateEntity(gatewayKeyResolver);
    GatewayKeyResolverPolicy updated = keyResolverPolicyRepository.save(gatewayKeyResolver);
    return KeyResolverPolicyResponse.fromEntity(updated);
  }
}
