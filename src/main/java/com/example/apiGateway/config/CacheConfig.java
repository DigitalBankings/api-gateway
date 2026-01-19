package com.example.apiGateway.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${cache.type:redis}") // choose "redis" or "caffeine"
    private String cacheType;

    @Bean
    public CacheManager cacheManager(Optional<RedisConnectionFactory> redisConnectionFactory) {
        return switch (cacheType.toLowerCase()) {
            case "redis" -> buildRedisCacheManager(
                    redisConnectionFactory.orElseThrow(() -> new IllegalStateException("RedisConnectionFactory required")));
            case "caffeine" -> buildCaffeineCacheManager();
            default -> throw new IllegalArgumentException("Unsupported cache type: " + cacheType);
        };
    }

    private CacheManager buildRedisCacheManager(RedisConnectionFactory factory) {
        ObjectMapper mapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();

        // enable polymorphic type info to avoid LinkedHashMap issues
        mapper.activateDefaultTyping(
                mapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
        serializer.setObjectMapper(mapper);

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .disableCachingNullValues()
                .entryTtl(Duration.ofMinutes(5));

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConfig)
                .build();
    }

    private CacheManager buildCaffeineCacheManager() {
        CaffeineCache authCodeCache = new CaffeineCache(
                CacheNames.GATEWAY_ROUTE,
                Caffeine.newBuilder()
                        .expireAfterWrite(5, TimeUnit.MINUTES)
                        .maximumSize(10_000)
                        .build());

        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(authCodeCache));
        return manager;
    }

}
