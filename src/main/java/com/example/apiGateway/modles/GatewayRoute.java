package com.example.apiGateway.modles;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "gateway_route")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GatewayRoute {

  @Id
  @Column(length = 36)
  private String id; // UUID string

  @Column(name = "route_code", nullable = false, unique = true)
  private String routeCode;

  @Column(nullable = false)
  private String paths;

  @Column(name = "target_uri", nullable = false)
  private String targetUri;

  @Column(name = "http_method", nullable = false)
  private String httpMethod;

  @Column(name = "time_out_ms", nullable = false)
  private Integer timeOutMs = Integer.valueOf("3000");

  @Column(name = "auth_required", nullable = false)
  private Boolean authRequired;

  @Column(nullable = false)
  private String status = "ACTIVE";

  @CreationTimestamp private LocalDateTime createdAt;

  @UpdateTimestamp private LocalDateTime updatedAt;
}
