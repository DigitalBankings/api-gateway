package com.example.apigateway.repositories;

import com.example.apigateway.enums.Status;
import com.example.apigateway.modles.GatewayRoutePolicyMap;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface GatewayRoutePolicyMapRepository
    extends JpaRepository<GatewayRoutePolicyMap, Long>,
        JpaSpecificationExecutor<GatewayRoutePolicyMap> {
  Optional<GatewayRoutePolicyMap> findFirstByRouteIdAndStatus(Long routeId, Status status);
}
