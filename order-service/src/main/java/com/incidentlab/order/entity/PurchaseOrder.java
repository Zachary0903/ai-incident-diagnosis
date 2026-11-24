package com.incidentlab.order.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** A placed order (table {@code orders}; named PurchaseOrder because ORDER is a SQL/JPQL keyword). */
@Entity
@Table(name = "orders")
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32)
    private String sku;

    @Column(nullable = false)
    private int qty;

    @Column(name = "promo_code", length = 32)
    private String promoCode;

    /** Price per unit at the moment the order was placed. */
    @Column(name = "unit_price_cents", nullable = false)
    private long unitPriceCents;

    @Column(name = "total_cents", nullable = false)
    private long totalCents;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    protected PurchaseOrder() {
    }

    public PurchaseOrder(String sku, int qty, String promoCode, long unitPriceCents, long totalCents) {
        this.sku = sku;
        this.qty = qty;
        this.promoCode = promoCode;
        this.unitPriceCents = unitPriceCents;
        this.totalCents = totalCents;
    }

    public Long getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public int getQty() {
        return qty;
    }

    public String getPromoCode() {
        return promoCode;
    }

    public long getUnitPriceCents() {
        return unitPriceCents;
    }

    public long getTotalCents() {
        return totalCents;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
