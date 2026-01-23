package com.example.api_gateway.repositories;

import com.example.api_gateway.modles.GatewayRateLimitPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GatewayRateLimitPolicyRepository
    extends JpaRepository<GatewayRateLimitPolicy, String> {
}
