package com.incidentlab.order.service;

import com.incidentlab.common.fault.FaultRegistry;
import com.incidentlab.order.entity.OrderAudit;
import com.incidentlab.order.repository.OrderAuditRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.stereotype.Service;

/**
 * Writes an audit row per order event.
 *
 * <p>Fault F02 ({@code connection-leak}): the "batched audit" path opens its own
 * application-managed {@link EntityManager} and never closes it. Hibernate holds the JDBC
 * connection until the EntityManager is closed, so the Hikari pool (size 10) drains within a
 * few seconds of traffic. Disabling the fault does not return leaked connections; restart
 * order-service to recover.
 */
@Service
public class OrderAuditService {

    static final String FAULT_CONNECTION_LEAK = "connection-leak";

    private final OrderAuditRepository audits;
    private final EntityManagerFactory entityManagerFactory;
    private final FaultRegistry faults;

    public OrderAuditService(OrderAuditRepository audits, EntityManagerFactory entityManagerFactory,
                             FaultRegistry faults) {
        this.audits = audits;
        this.entityManagerFactory = entityManagerFactory;
        this.faults = faults;
        faults.register(FAULT_CONNECTION_LEAK);
    }

    public void record(long orderId, String event) {
        if (faults.isActive(FAULT_CONNECTION_LEAK)) {
            recordBatched(orderId, event);
        } else {
            audits.save(new OrderAudit(orderId, event));
        }
    }

    /** Bug: the EntityManager is kept "for the next batch" and never closed. */
    private void recordBatched(long orderId, String event) {
        EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        em.persist(new OrderAudit(orderId, event));
        em.getTransaction().commit();
    }
}
