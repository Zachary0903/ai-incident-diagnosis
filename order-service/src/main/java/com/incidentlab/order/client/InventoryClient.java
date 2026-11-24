package com.incidentlab.order.client;

import com.incidentlab.order.dto.ProductResponse;
import com.incidentlab.order.dto.ReserveStockRequest;
import com.incidentlab.order.exception.UnknownProductException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Calls to inventory-service, which owns the product catalog (prices) and stock.
 *
 * <p>Errors other than an unknown SKU propagate as {@code RestClientException}s and are mapped
 * to HTTP responses by {@code GlobalExceptionHandler}.
 */
@Component
public class InventoryClient {

    private final RestClient inventoryRestClient;

    public InventoryClient(RestClient inventoryRestClient) {
        this.inventoryRestClient = inventoryRestClient;
    }

    /** @throws UnknownProductException if inventory-service has no such SKU */
    public ProductResponse getProduct(String sku) {
        return inventoryRestClient.get()
                .uri("/products/{sku}", sku)
                .retrieve()
                .onStatus(status -> status.value() == HttpStatus.NOT_FOUND.value(), (request, response) -> {
                    throw new UnknownProductException(sku);
                })
                .body(ProductResponse.class);
    }

    /** Reserves stock; inventory-service answers 409 if the SKU is unknown or out of stock. */
    public void reserveStock(String sku, int qty) {
        inventoryRestClient.post()
                .uri("/inventory/reserve")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ReserveStockRequest(sku, qty))
                .retrieve()
                .toBodilessEntity();
    }
}
