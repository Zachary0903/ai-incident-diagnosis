package com.incidentlab.order.dto;

/** Response of {@code POST /orders}; also the payload of the {@code order-created} Kafka event. */
public record OrderResponse(long orderId, String sku, int qty, long unitPriceCents, long totalCents) {}
