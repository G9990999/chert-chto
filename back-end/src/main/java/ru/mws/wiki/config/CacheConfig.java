package ru.mws.wiki.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Caffeine cache configuration.
 *
 * <p>Caches:
 * <ul>
 *   <li>{@code pages} — individual page responses, 5 min TTL</li>
 *   <li>{@code pageList} — page list responses, 2 min TTL</li>
 *   <li>{@code tables} — MWS Tables API responses, 5 min TTL</li>
 * </ul>
 * </p>
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Creates the Caffeine cache manager with named caches.
     *
     * @return CacheManager
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager("pages", "pageList", "tables");
        manager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .recordStats());
        return manager;
    }
}
