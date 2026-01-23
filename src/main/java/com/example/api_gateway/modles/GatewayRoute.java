package com.example.api_gateway.modles;

import com.example.api_gateway.enums.RouteStatus;
import jakarta.persistence.*;

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
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;   // BIGINT PK (auto-generated)

  @Column(name = "service_name", nullable = false, length = 100)
  private String serviceName;

  @Column(name = "route_code", nullable = false, unique = true, length = 100)
  private String routeCode;   // Human-readable code

  @Column(name = "path", nullable = false, length = 255)
  private String path;

  @Column(name = "target_uri", nullable = false, length = 255)
  private String targetUri;

  @Column(name = "http_method", nullable = false, length = 20)
  private String httpMethod;

  @Column(name = "time_out_ms", nullable = false)
  private Integer timeOutMs = 30000;

  @Column(name = "auth_required", nullable = false)
  private Boolean authRequired = Boolean.TRUE;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private RouteStatus status = RouteStatus.ACTIVE;

  @CreationTimestamp
  @Column(updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  private LocalDateTime updatedAt;
}
