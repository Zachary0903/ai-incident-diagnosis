package com.incidentlab.order.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** One lifecycle event of an order (table {@code order_audit}). */
@Entity
@Table(name = "order_audit")
public class OrderAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private long orderId;

    @Column(nullable = false, length = 32)
    private String event;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    protected OrderAudit() {
    }

    public OrderAudit(long orderId, String event) {
        this.orderId = orderId;
        this.event = event;
    }

    public Long getId() {
        return id;
    }

    public long getOrderId() {
        return orderId;
    }

    public String getEvent() {
        return event;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
