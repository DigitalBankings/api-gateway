package com.example.apigateway.controller;

import com.example.apigateway.dto.keyresolverpolicy.CreateKeyResolverPolicyRequest;
import com.example.apigateway.dto.keyresolverpolicy.KeyResolverPolicyResponse;
import com.example.apigateway.services.KeyResolverPolicyService;
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
    KeyResolverPolicyResponse response = keyResolverPolicyService.create(request);
    //    SeyhaLogger.RequestInfo reqInfo = new SeyhaLogger.RequestInfo();
    //    reqInfo.setEndpoint("/v1/key-resolvers/register");
    //    reqInfo.setMethod("POST");
    //    reqInfo.setBody(request); // your DTO
    //    reqInfo.setRemoteAddress("127.0.0.1"); // optional
    //    reqInfo.setHost("localhost"); // optional
    //    reqInfo.setUserAgent("Postman"); // optional
    //
    //    SeyhaLogger.ResponseInfo resInfo = new SeyhaLogger.ResponseInfo();
    //    resInfo.setStatus(200); // HTTP status
    //    resInfo.setBody(response); // your DTO

    //    seyhaLogger.log("RegisterKeyResolver", reqInfo, resInfo);
    return response;
  }
}
