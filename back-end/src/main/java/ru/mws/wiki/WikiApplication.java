package ru.mws.wiki;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main application entry point for MWS Wiki — a collaborative wiki editor
 * integrated with MWS Tables API.
 *
 * <p>Features virtual threads (Project Loom), Caffeine caching, Resilience4j
 * circuit breaker, and real-time collaborative editing via STOMP/WebSocket.</p>
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
public class WikiApplication {

    /**
     * Application entry point.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(WikiApplication.class, args);
    }
}
