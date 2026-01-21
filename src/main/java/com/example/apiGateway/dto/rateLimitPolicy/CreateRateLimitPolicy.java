package com.example.apiGateway.dto.rateLimitPolicy;

import com.example.apiGateway.enums.Status;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

public class CreateRateLimitPolicy {

  @NotNull private Integer id;

  @NotNull private String policyCode;

  private Integer replenishRate;
  private Integer burstCapacity;
  private Integer burstRate;
  private String keyResolverPolicyId;
  private Status status = Status.ACTIVE;

  @CreationTimestamp private LocalDateTime createdAt;

  @UpdateTimestamp private LocalDateTime updatedAt;
}
