package com.example.apiGateway.modles;


import com.example.apiGateway.enums.Status;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "gateway_rate_limit_policy")
@Data
public class GatewayRateLimitPolicy {

    @Id
    @Column(length = 36)
    private String id; // UUID string

    @Column( length = 50 , nullable = false, unique = true, name = "policy_code")
    private String policyCode;

    @Column(name = "replenish_rate")
    private Integer replenishRate;

    @Column(name = "burst_capacity")
    private Integer burstCapacity;

    @Column(name = "window_seconds")
    private Integer windowSeconds;

    @Column(name = "key_resolver_policy_id")
    private String keyResolverPolicyId;

    @Column(name = "status")
    private Status status = Status.ACTIVE;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;


}
