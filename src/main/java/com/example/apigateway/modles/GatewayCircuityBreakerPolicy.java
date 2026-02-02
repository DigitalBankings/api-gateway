package com.example.apigateway.modles;

import com.example.apigateway.enums.SlidingWindowType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "gateway_circuit_breaker_policy")
@Getter
@Setter
public class GatewayCircuityBreakerPolicy {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "cb_name", nullable = false, unique = true)
  private String cbName;

  @Enumerated(EnumType.STRING)
  @Column(name = "sliding_window_type")
  private SlidingWindowType slidingWindowType;

  @Column(name = "sliding_window_size")
  private Integer slidingWindowSize;

  @Column(name = "failure_rate_threshold")
  private Integer failureRateThreshold;

  @Column(name = "slow_call_rate_threshold")
  private Integer slowCallRateThreshold;

  @Column(name = "slow_call_duration_ms")
  private Integer slowCallDurationMs;

  @Column(name = "open_state_wait_ms")
  private Integer openStateWaitMs;

  @Column(name = "half_open_calls")
  private Integer halfOpenCalls;

  @Column(name = "timeout_ms")
  private Integer timeoutMs;

  @CreationTimestamp private LocalDateTime createdAt;
  @UpdateTimestamp private LocalDateTime updatedAt;
}
