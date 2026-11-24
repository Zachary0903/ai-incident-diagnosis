package com.incidentlab.order.dto;

/** Body of {@code POST /orders}. */
public record CreateOrderRequest(String sku, int qty, String promoCode) {}
