package com.example.apigateway.repositories;

import com.example.apigateway.modles.GatewayRoutePolicyMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GatewayRoutePolicyMapRepository extends JpaRepository<GatewayRoutePolicyMap, Long> {
    Optional<GatewayRoutePolicyMap> findByRouteId(Long routeId);
}
