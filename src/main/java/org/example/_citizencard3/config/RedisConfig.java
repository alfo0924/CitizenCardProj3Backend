package org.example._citizencard3.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import redis.embedded.RedisServer;

@Configuration
public class RedisConfig {

  private RedisServer redisServer;

  @PostConstruct
  public void startRedis() throws Exception {
    try {
      redisServer = new RedisServer(6379);
      redisServer.start();
    } catch (Exception e) {
      // 如果已經有 Redis 在運行，就不用再啟動
      if (!e.getMessage().contains("Address already in use")) {
        throw e;
      }
    }
  }


  @PreDestroy
  public void stopRedis() {
    if (redisServer != null) {
      try {
        redisServer.stop();
      } catch (Exception e) {
        // 忽略停止時的錯誤，因為可能 Redis 已經停止
        System.out.println("Redis server stop error: " + e.getMessage());
      }
    }
  }


  @Bean
  public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(factory);

    ObjectMapper mapper = JsonMapper.builder()
        .findAndAddModules()
        .build();

    Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer =
        new Jackson2JsonRedisSerializer<>(mapper, Object.class);

    StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
    template.setKeySerializer(stringRedisSerializer);
    template.setHashKeySerializer(stringRedisSerializer);
    template.setValueSerializer(jackson2JsonRedisSerializer);
    template.setHashValueSerializer(jackson2JsonRedisSerializer);

    template.afterPropertiesSet();
    return template;
  }
}