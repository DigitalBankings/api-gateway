//
// package com.example.apigateway.config;
//
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.seyha.common.logger.SeyhaLogger;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
//
// @Configuration
// public class SeyhaLoggerConfiguration {
//
//    private final ObjectMapper objectMapper;
//
//    public SeyhaLoggerConfiguration(ObjectMapper objectMapper) {
//        this.objectMapper = objectMapper;
//    }
//
//    @Bean
//    public SeyhaLogger seyhaLogger() {
//        return new SeyhaLogger(objectMapper);
//    }
// }
//
