package com.example.frauddetectionsystem.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        // Configure cache for active rules - optimized for 30K-40K rules
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(50000) // Allow for growth beyond 40K rules
            .expireAfterWrite(5, TimeUnit.MINUTES) // Refresh rules every 5 minutes
            .recordStats()); // Enable cache statistics for monitoring
        
        // Register cache names
        cacheManager.setCacheNames(java.util.List.of("activeRules", "rulesByType", "transactionCache"));
        
        return cacheManager;
    }
}
