package com.example.apiGateway.repositories;


import com.example.apiGateway.modles.GatewayKeyResolverPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GatewayKeyResolverPolicyRepository extends JpaRepository<GatewayKeyResolverPolicy, String> {

    Optional<GatewayKeyResolverPolicy> findByPolicyCode(String policyCode);
    boolean existsByPolicyCode(String policyCode);

}
