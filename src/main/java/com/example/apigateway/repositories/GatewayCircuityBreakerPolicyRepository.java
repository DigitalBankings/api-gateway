package com.example.apigateway.repositories;

import com.example.apigateway.modles.GatewayCircuityBreakerPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GatewayCircuityBreakerPolicyRepository
    extends JpaRepository<GatewayCircuityBreakerPolicy, Long> {}
