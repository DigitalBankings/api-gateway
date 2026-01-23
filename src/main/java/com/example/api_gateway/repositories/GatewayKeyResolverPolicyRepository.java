package com.example.api_gateway.repositories;

import com.example.api_gateway.modles.GatewayKeyResolverPolicy;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GatewayKeyResolverPolicyRepository
    extends JpaRepository<GatewayKeyResolverPolicy, Long> {

  Optional<GatewayKeyResolverPolicy> findByPolicyCode(String policyCode);

  boolean existsByPolicyCode(String policyCode);
}
