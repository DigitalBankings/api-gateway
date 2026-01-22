package com.example.apiGateway.repositories;

import com.example.apiGateway.modles.GatewayRoute;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GatewayRouteRepository extends JpaRepository<GatewayRoute, Long> {
  Optional<GatewayRoute> findByRouteCode(String routeCode);
}
