package com.samsamhajo.deepground.support;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import org.springframework.boot.test.context.TestConfiguration;
import redis.embedded.RedisServer;

@TestConfiguration
public class TestRedisConfig {

    private RedisServer redisServer;
    private static final int REDIS_PORT = 63790;

    @PostConstruct
    public void startRedis() throws IOException {
        redisServer = new RedisServer(REDIS_PORT);
        redisServer.start();
    }

    @PreDestroy
    public void stopRedis() throws IOException {
        if (redisServer != null) {
            redisServer.stop();
        }
    }
}
