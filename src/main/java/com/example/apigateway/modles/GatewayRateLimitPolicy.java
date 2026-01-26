package com.example.apigateway.modles;

import com.example.apigateway.enums.Status;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "gateway_rate_limit_policy")
@Data
public class GatewayRateLimitPolicy {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id; // UUID string

  @Column(name = "replenish_rate")
  private Integer replenishRate;

  @Column(name = "burst_capacity")
  private Integer burstCapacity;

  @Column(name = "window_seconds")
  private Integer windowSeconds;

  @Column(name = "key_resolver_policy_id")
  private Long keyResolverPolicyId;

  @Enumerated(EnumType.STRING)
  @Column(name = "status")
  private Status status = Status.ACTIVE;

  @CreationTimestamp private LocalDateTime createdAt;
  @UpdateTimestamp private LocalDateTime updatedAt;
}
