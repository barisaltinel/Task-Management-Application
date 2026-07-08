package io.github.barisaltinel.taskmanagement.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@EnableCaching
@EnableConfigurationProperties(RedisCacheProperties.class)
public class TaskManagementCacheConfig {

    @Bean("cacheManager")
    @ConditionalOnProperty(prefix = "app.redis", name = "enabled", havingValue = "true")
    public CacheManager redisCacheManager(
            RedisConnectionFactory redisConnectionFactory,
            RedisCacheProperties redisCacheProperties,
            ObjectMapper objectMapper
    ) {
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        RedisCacheConfiguration defaultConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(serializer)
                )
                .entryTtl(redisCacheProperties.getCacheTtl())
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigurations = new LinkedHashMap<>();
        TaskManagementCacheNames.all().forEach(cacheName -> cacheConfigurations.put(cacheName, defaultConfiguration));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfiguration)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

    @Bean("cacheManager")
    @ConditionalOnMissingBean(CacheManager.class)
    public CacheManager localCacheManager() {
        return new ConcurrentMapCacheManager(TaskManagementCacheNames.all().toArray(String[]::new));
    }
}
