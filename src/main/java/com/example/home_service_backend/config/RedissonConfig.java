package com.example.home_service_backend.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 使用 Spring Boot 官方 RedisProperties 创建 Redisson 客户端，
 * 避免散落连接参数。
 */
@Configuration
public class RedissonConfig {

    @Bean(destroyMethod = "shutdown")
    RedissonClient redissonClient(RedisProperties properties) {
        Config config = new Config();

        String address = "redis://" + properties.getHost() + ":" + properties.getPort();

        config.useSingleServer()
                .setAddress(address)
                .setDatabase(properties.getDatabase());

        if (properties.getPassword() != null
                && !properties.getPassword().isBlank()) {
            config.useSingleServer()
                    .setPassword(properties.getPassword());
        }

        return Redisson.create(config);
    }
}