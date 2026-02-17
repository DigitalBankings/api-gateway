package com.example.apigateway.repositories;

import com.example.apigateway.modles.GatewayKeyResolverPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GatewayKeyResolverPolicyRepository
    extends JpaRepository<GatewayKeyResolverPolicy, Long> {

  boolean existsByPolicyCode(String policyCode);

  Page<GatewayKeyResolverPolicy> findAll(Pageable pageable);
}
