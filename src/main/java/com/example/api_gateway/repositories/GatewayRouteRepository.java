package com.example.api_gateway.repositories;

import com.example.api_gateway.modles.GatewayRoute;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GatewayRouteRepository extends JpaRepository<GatewayRoute, Long> {
  Optional<GatewayRoute> findByRouteCode(String routeCode);
}
