package com.example.apigateway.repositories;

import com.example.apigateway.modles.GatewayRoutePolicyMap;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GatewayRoutePolicyMapRepository
    extends JpaRepository<GatewayRoutePolicyMap, Long> {
  Optional<GatewayRoutePolicyMap> findByRouteId(Long routeId);
}
