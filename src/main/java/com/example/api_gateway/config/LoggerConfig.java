package com.example.api_gateway.config;

import com.seyha.common.logger.SeyhaLogger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoggerConfig {

  @Bean
  public SeyhaLogger seyhaLogger() {
    return new SeyhaLogger(); // you can set default configs if needed
  }
}
