package com.example.apiGateway.controller;


import com.example.apiGateway.dto.keyResolverPolicy.CreateKeyResolverPolicyRequest;
import com.example.apiGateway.dto.keyResolverPolicy.KeyResolverPolicyResponse;
import com.example.apiGateway.services.KeyResolverPolicyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/key-resolvers")
@RequiredArgsConstructor
public class KeyResolverController {

    private final KeyResolverPolicyService keyResolverPolicyService;

    @PostMapping("register")
    public KeyResolverPolicyResponse create(@RequestBody CreateKeyResolverPolicyRequest request) {
       return keyResolverPolicyService.create(request);
    }
}
