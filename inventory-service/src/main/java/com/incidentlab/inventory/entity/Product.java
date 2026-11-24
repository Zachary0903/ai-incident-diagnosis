package com.incidentlab.inventory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** A catalog product with its current list price and stock level (table {@code product}). */
@Entity
@Table(name = "product")
public class Product {

    @Id
    @Column(length = 32)
    private String sku;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(name = "price_cents", nullable = false)
    private long priceCents;

    @Column(nullable = false)
    private long stock;

    protected Product() {
    }

    public Product(String sku, String name, long priceCents, long stock) {
        this.sku = sku;
        this.name = name;
        this.priceCents = priceCents;
        this.stock = stock;
    }

    /**
     * Takes {@code qty} units out of stock if enough are available.
     * Callers must hold a write lock on this row (see {@link com.incidentlab.inventory.repository.ProductRepository#findWithLockBySku}).
     *
     * @return false (and leaves stock unchanged) if there is not enough stock
     */
    public boolean reserve(int qty) {
        if (stock < qty) {
            return false;
        }
        stock -= qty;
        return true;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public long getPriceCents() {
        return priceCents;
    }

    public long getStock() {
        return stock;
    }
}
