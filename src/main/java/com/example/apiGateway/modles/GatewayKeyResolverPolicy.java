package com.example.apiGateway.modles;
import com.example.apiGateway.enums.KeyResolverStrategy;
import com.example.apiGateway.enums.Status;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Table
@Entity(name =  "gateway_key_resolver_policy")
public class GatewayKeyResolverPolicy {

    @Id
    @Column(length = 36)
    private String id; // UUID string

    @Column( length = 50, name = "policy_code")
    private String policyCode;

    @Column(  name = "strategy", length = 30 , nullable = false)
    private KeyResolverStrategy  strategy = KeyResolverStrategy.API_KEY ;

    @Column( name = "header_name", length = 100)
    private String headerName ;

    @Column( name = "fallback_strategy", length = 30)
    private KeyResolverStrategy fallbackStrategy ;

    @Enumerated(EnumType.STRING)
    @Column( name = "status", nullable = false)
    private Status status;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
