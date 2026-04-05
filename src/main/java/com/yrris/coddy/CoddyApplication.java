package com.yrris.coddy;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication(exclude = {RedisEmbeddingStoreAutoConfiguration.class})
public class CoddyApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoddyApplication.class, args);
    }

}
