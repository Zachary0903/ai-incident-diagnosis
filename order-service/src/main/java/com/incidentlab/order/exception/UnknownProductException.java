package com.incidentlab.order.exception;

/** inventory-service's catalog has no such SKU, so it cannot be priced or ordered. */
public class UnknownProductException extends RuntimeException {

    private final String sku;

    public UnknownProductException(String sku) {
        super("No price for sku " + sku);
        this.sku = sku;
    }

    public String getSku() {
        return sku;
    }
}
