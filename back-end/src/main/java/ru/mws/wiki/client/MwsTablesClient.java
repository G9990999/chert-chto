package ru.mws.wiki.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.CompletableFuture;

/**
 * Client for the MWS Tables external API.
 *
 * <p>All calls are protected by a Resilience4j circuit breaker named
 * {@code mwsTablesApi} and a time limiter of 5 seconds. GET calls
 * are cached via Caffeine to reduce external API load.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MwsTablesClient {

    private final WebClient mwsTablesWebClient;

    @Value("${mws.tables.space-id}")
    private String spaceId;

    /**
     * Fetches all datasheets (tables) in the configured space.
     *
     * @return raw JSON response as String
     */
    @Cacheable(value = "tables", key = "'datasheets-' + #spaceId")
    @CircuitBreaker(name = "mwsTablesApi", fallbackMethod = "fallbackJson")
    @TimeLimiter(name = "mwsTablesApi")
    public CompletableFuture<String> getDatasheets() {
        log.debug("Fetching datasheets for space: {}", spaceId);
        return mwsTablesWebClient.get()
                .uri("/spaces/{spaceId}/datasheets", spaceId)
                .retrieve()
                .bodyToMono(String.class)
                .toFuture();
    }

    /**
     * Fetches fields for a specific datasheet.
     *
     * @param dstId datasheet ID
     * @return raw JSON response as String
     */
    @Cacheable(value = "tables", key = "'fields-' + #dstId")
    @CircuitBreaker(name = "mwsTablesApi", fallbackMethod = "fallbackJson")
    @TimeLimiter(name = "mwsTablesApi")
    public CompletableFuture<String> getFields(String dstId) {
        log.debug("Fetching fields for datasheet: {}", dstId);
        return mwsTablesWebClient.get()
                .uri("/datasheets/{dstId}/fields", dstId)
                .retrieve()
                .bodyToMono(String.class)
                .toFuture();
    }

    /**
     * Fetches records from a datasheet with optional pagination.
     *
     * @param dstId    datasheet ID
     * @param pageNum  page number (1-based)
     * @param pageSize number of records per page
     * @return raw JSON response as String
     */
    @Cacheable(value = "tables", key = "'records-' + #dstId + '-' + #pageNum + '-' + #pageSize")
    @CircuitBreaker(name = "mwsTablesApi", fallbackMethod = "fallbackJson")
    @TimeLimiter(name = "mwsTablesApi")
    public CompletableFuture<String> getRecords(String dstId, int pageNum, int pageSize) {
        log.debug("Fetching records for datasheet: {}, page: {}, size: {}", dstId, pageNum, pageSize);
        return mwsTablesWebClient.get()
                .uri(uri -> uri
                        .path("/datasheets/{dstId}/records")
                        .queryParam("pageNum", pageNum)
                        .queryParam("pageSize", pageSize)
                        .build(dstId))
                .retrieve()
                .bodyToMono(String.class)
                .toFuture();
    }

    /**
     * Fetches views for a datasheet.
     *
     * @param dstId datasheet ID
     * @return raw JSON response as String
     */
    @Cacheable(value = "tables", key = "'views-' + #dstId")
    @CircuitBreaker(name = "mwsTablesApi", fallbackMethod = "fallbackJson")
    @TimeLimiter(name = "mwsTablesApi")
    public CompletableFuture<String> getViews(String dstId) {
        log.debug("Fetching views for datasheet: {}", dstId);
        return mwsTablesWebClient.get()
                .uri("/datasheets/{dstId}/views", dstId)
                .retrieve()
                .bodyToMono(String.class)
                .toFuture();
    }

    /**
     * Fallback method for circuit breaker — returns a JSON error payload.
     *
     * @param ex the exception that triggered the fallback
     * @return CompletableFuture with an error JSON string
     */
    public CompletableFuture<String> fallbackJson(Exception ex) {
        log.warn("MWS Tables API call failed, using fallback: {}", ex.getMessage());
        return CompletableFuture.completedFuture(
                "{\"success\":false,\"code\":500,\"message\":\"MWS Tables API is temporarily unavailable\"}");
    }
}
