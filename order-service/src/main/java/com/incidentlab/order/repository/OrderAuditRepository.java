package com.incidentlab.order.repository;

import java.util.List;

import com.incidentlab.order.entity.OrderAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderAuditRepository extends JpaRepository<OrderAudit, Long> {

    List<OrderAudit> findByOrderIdOrderByIdAsc(long orderId);
}
