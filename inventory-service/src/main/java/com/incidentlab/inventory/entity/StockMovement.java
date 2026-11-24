package com.incidentlab.inventory.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** One change to a SKU's stock: negative for reservations, positive for restocks (table {@code stock_movement}). */
@Entity
@Table(name = "stock_movement")
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32)
    private String sku;

    @Column(nullable = false)
    private int qty;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    protected StockMovement() {
    }

    public StockMovement(String sku, int qty) {
        this.sku = sku;
        this.qty = qty;
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

    public Instant getCreatedAt() {
        return createdAt;
    }
}
