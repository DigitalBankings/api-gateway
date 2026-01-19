package com.example.ABCBank.apiGateway.repositories;

import com.example.ABCBank.apiGateway.modles.GatewayRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GatewayRouteRepository extends JpaRepository<GatewayRoute, String> {
    Optional<GatewayRoute> findByRouteCode(String routeCode);
}
