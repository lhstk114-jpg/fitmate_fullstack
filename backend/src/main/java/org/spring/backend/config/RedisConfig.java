package org.spring.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    //redis의 host(주소)
    @Value("${spring.data.redis.host}")
    private String host;

    //redis의 port번호
    @Value("${spring.data.redis.port}")
    private int port;

    //Redis 연결 팩토리 설정
    @Bean
    public RedisConnectionFactory redisConnectionFactory(){
        // Redis 설정
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(host);
        redisStandaloneConfiguration.setPort(port);

        //Lettuce 와 Jedis 중 Lettuce가 성능이 더 좋기에 Lettuce사용(비동기처리)
        return new LettuceConnectionFactory(redisStandaloneConfiguration);
    }

    //RedisTemplate 사용을 위해 추가
    //RedisTemplate는 DB처럼 Set, Get, Delete 등의 기능이 포함되어있음
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory redisConnectionFactory){
        //Redis와 통신할 때 사용할 탬플릿 설정
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        //key, value에 대한 직렬화 설정
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());

        //hash key, hash value에 대한 직렬화 설정
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());

        return redisTemplate;
    }
}
