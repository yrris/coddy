package com.yrris.coddy.config;

import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "app.ai", name = "provider", havingValue = "langchain4j")
public class RedisChatMemoryStoreConfig {

    @Bean
    public RedisChatMemoryStore redisChatMemoryStore(
            @Value("${spring.data.redis.host:localhost}") String host,
            @Value("${spring.data.redis.port:6379}") Integer port,
            @Value("${spring.data.redis.password:}") String password
    ) {
        RedisChatMemoryStore.Builder builder = RedisChatMemoryStore.builder()
                .host(host)
                .port(port)
                .prefix("coddy:chat_memory:")
                .ttl(7 * 24 * 60 * 60L); // 7 days in seconds

        if (password != null && !password.isBlank()) {
            builder.password(password);
        }

        return builder.build();
    }
}
