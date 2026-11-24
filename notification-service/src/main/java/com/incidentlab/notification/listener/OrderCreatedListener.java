package com.incidentlab.notification.listener;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.incidentlab.notification.service.NotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/** Consumes {@code order-created} (single consumer thread; see fault F03). */
@Component
public class OrderCreatedListener {

    private final NotificationService notifications;
    private final ObjectMapper mapper;

    public OrderCreatedListener(NotificationService notifications, ObjectMapper mapper) {
        this.notifications = notifications;
        this.mapper = mapper;
    }

    @KafkaListener(topics = "order-created")
    public void onOrderCreated(String payload) throws Exception {
        JsonNode order = mapper.readTree(payload);
        notifications.sendOrderConfirmation(order.path("orderId").asLong());
    }
}
