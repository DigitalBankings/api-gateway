package com.example.apigateway.repositories;

import com.example.apigateway.modles.GatewayRateLimitPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GatewayRateLimitPolicyRepository
    extends JpaRepository<GatewayRateLimitPolicy, Long> {
  GatewayRateLimitPolicy findById(long id);
}
