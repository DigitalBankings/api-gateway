package com.example.apigateway.modles;

import com.example.apigateway.enums.KeyResolverStrategy;
import com.example.apigateway.enums.Status;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Data
@Table
@Entity(name = "gateway_key_resolver_policy")
public class GatewayKeyResolverPolicy {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id; // UUID string

  @Column(length = 50, name = "policy_code")
  private String policyCode;

  @Enumerated(EnumType.STRING)
  @Column(name = "strategy", nullable = false)
  private KeyResolverStrategy strategy;

  @Column(name = "header_name", length = 100)
  private String headerName;

  @Enumerated(EnumType.STRING)
  @Column(name = "fallback_strategy")
  private KeyResolverStrategy fallbackStrategy;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private Status status;

  @CreationTimestamp private LocalDateTime createdAt;
  @UpdateTimestamp private LocalDateTime updatedAt;
}
