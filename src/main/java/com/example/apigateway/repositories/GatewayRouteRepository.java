package com.example.apigateway.repositories;

import com.example.apigateway.modles.GatewayRoute;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GatewayRouteRepository extends JpaRepository<GatewayRoute, Long> {
  Optional<GatewayRoute> findByRouteCode(String routeCode);

  Page<GatewayRoute> findAll(Pageable pageable);
}
