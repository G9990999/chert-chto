package ru.mws.wiki.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.mws.wiki.client.MwsTablesClient;

import java.util.concurrent.CompletableFuture;

/**
 * REST controller that proxies requests to the MWS Tables external API.
 *
 * <p>All calls are protected by circuit breaker + time limiter
 * (defined in {@link ru.mws.wiki.client.MwsTablesClient}).
 * GET responses are cached by Caffeine.</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/tables")
@RequiredArgsConstructor
public class TablesController {

    private final MwsTablesClient tablesClient;

    /**
     * Lists all datasheets in the configured MWS Tables space.
     *
     * @return raw JSON from MWS Tables API
     */
    @GetMapping
    public CompletableFuture<String> getDatasheets() {
        return tablesClient.getDatasheets();
    }

    /**
     * Returns fields of a specific datasheet.
     *
     * @param dstId datasheet ID
     * @return raw JSON from MWS Tables API
     */
    @GetMapping("/{dstId}/fields")
    public CompletableFuture<String> getFields(@PathVariable String dstId) {
        return tablesClient.getFields(dstId);
    }

    /**
     * Returns records of a datasheet with pagination.
     *
     * @param dstId    datasheet ID
     * @param pageNum  page number (default 1)
     * @param pageSize records per page (default 100)
     * @return raw JSON from MWS Tables API
     */
    @GetMapping("/{dstId}/records")
    public CompletableFuture<String> getRecords(
            @PathVariable String dstId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "100") int pageSize) {
        return tablesClient.getRecords(dstId, pageNum, pageSize);
    }

    /**
     * Returns views of a datasheet.
     *
     * @param dstId datasheet ID
     * @return raw JSON from MWS Tables API
     */
    @GetMapping("/{dstId}/views")
    public CompletableFuture<String> getViews(@PathVariable String dstId) {
        return tablesClient.getViews(dstId);
    }
}
