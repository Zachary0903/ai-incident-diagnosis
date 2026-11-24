package com.incidentlab.order.service;

import tools.jackson.databind.ObjectMapper;
import com.incidentlab.order.client.InventoryClient;
import com.incidentlab.order.dto.CreateOrderRequest;
import com.incidentlab.order.dto.OrderResponse;
import com.incidentlab.order.entity.PurchaseOrder;
import com.incidentlab.order.model.PriceQuote;
import com.incidentlab.order.repository.PurchaseOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class OrderService {

    public static final String ORDER_CREATED_TOPIC = "order-created";

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final InventoryClient inventory;
    private final PurchaseOrderRepository orders;
    private final PricingService pricing;
    private final OrderAuditService audit;
    private final TransactionTemplate tx;
    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper mapper;

    public OrderService(InventoryClient inventory, PurchaseOrderRepository orders, PricingService pricing,
                        OrderAuditService audit, TransactionTemplate tx,
                        KafkaTemplate<String, String> kafka, ObjectMapper mapper) {
        this.inventory = inventory;
        this.orders = orders;
        this.pricing = pricing;
        this.audit = audit;
        this.tx = tx;
        this.kafka = kafka;
        this.mapper = mapper;
    }

    public OrderResponse placeOrder(CreateOrderRequest request) {
        PriceQuote quote = pricing.quote(request.sku(), request.qty(), request.promoCode());

        // Outside any DB transaction: don't hold a pooled connection while waiting on inventory.
        inventory.reserveStock(request.sku(), request.qty());

        // Order row and its audit row commit together.
        PurchaseOrder order = tx.execute(status -> {
            PurchaseOrder saved = orders.save(new PurchaseOrder(
                    request.sku(), request.qty(), request.promoCode(),
                    quote.unitPriceCents(), quote.totalCents()));
            audit.record(saved.getId(), "CREATED");
            return saved;
        });

        OrderResponse response = new OrderResponse(order.getId(), order.getSku(), order.getQty(),
                order.getUnitPriceCents(), order.getTotalCents());
        kafka.send(ORDER_CREATED_TOPIC, String.valueOf(order.getId()), toJson(response));
        log.info("Order {} created for sku {} qty{} unit={} total={} cents",
                order.getId(), order.getSku(), order.getQty(), order.getUnitPriceCents(), order.getTotalCents());
        return response;
    }

    /** Jackson 3 exceptions are unchecked; a serialization failure fails the request. */
    private String toJson(OrderResponse response) {
        return mapper.writeValueAsString(response);
    }
}
