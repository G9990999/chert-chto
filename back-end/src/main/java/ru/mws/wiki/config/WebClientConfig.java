package ru.mws.wiki.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * WebClient configuration for calling the MWS Tables external API.
 *
 * <p>The client is pre-configured with the base URL and the Bearer token
 * for authorization.</p>
 */
@Configuration
public class WebClientConfig {

    @Value("${mws.tables.base-url}")
    private String baseUrl;

    @Value("${mws.tables.token}")
    private String token;

    /**
     * Creates a WebClient pre-configured for the MWS Tables API.
     *
     * @return WebClient instance
     */
    @Bean
    public WebClient mwsTablesWebClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + token)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
