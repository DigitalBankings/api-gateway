package com.example.apigateway.modles;

import com.example.apigateway.enums.Status;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "gateway_route_policy_map")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GatewayRoutePolicyMap {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "route_id", nullable = false)
  private Long routeId;

  @Column(name = "rate_limit_policy_id", nullable = false)
  private Long rateLimitPolicyId;

  @Column(name = "circuit_breaker_policy_id", nullable = false)
  private Long circuitBreakerPolicyId;

  @Enumerated(EnumType.STRING)
  @Column(name = "status")
  private Status status;

  @CreationTimestamp private LocalDateTime createdAt;

  @UpdateTimestamp private LocalDateTime updatedAt;
}
