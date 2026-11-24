package com.incidentlab.order.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/** HTTP client to inventory-service, with the timeouts fault F05 is built around. */
@Configuration
public class InventoryClientConfig {

    @Bean
    RestClient inventoryRestClient(@Value("${inventory.base-url}") String baseUrl,
                                   @Value("${inventory.connect-timeout-ms}") int connectTimeoutMs,
                                   @Value("${inventory.read-timeout-ms}") int readTimeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);
        return RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }
}
