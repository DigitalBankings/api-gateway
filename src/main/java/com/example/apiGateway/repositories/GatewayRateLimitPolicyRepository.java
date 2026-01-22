package com.example.apiGateway.repositories;

import com.example.apiGateway.modles.GatewayRateLimitPolicy;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GatewayRateLimitPolicyRepository
    extends JpaRepository<GatewayRateLimitPolicy, String> {
}
