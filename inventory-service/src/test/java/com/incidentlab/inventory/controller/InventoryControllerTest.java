package com.incidentlab.inventory.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import com.incidentlab.inventory.model.Reservation;
import com.incidentlab.inventory.model.StockLevel;
import com.incidentlab.inventory.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** The JSON contract order-service and the scenarios depend on. */
@WebMvcTest(InventoryController.class)
class InventoryControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean InventoryService inventory;

    @Test
    void reserveOk() throws Exception {
        when(inventory.reserve("SKU-0001", 2)).thenReturn(Optional.of(new Reservation("SKU-0001", 2, 7)));

        mvc.perform(post("/inventory/reserve").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\":\"SKU-0001\",\"qty\":2}"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"sku\":\"SKU-0001\",\"reserved\":2}", true));
    }

    @Test
    void reserveRejectedIs409() throws Exception {
        when(inventory.reserve("SKU-0001", 99)).thenReturn(Optional.empty());

        mvc.perform(post("/inventory/reserve").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\":\"SKU-0001\",\"qty\":99}"))
                .andExpect(status().isConflict())
                .andExpect(content().json("{\"error\":\"insufficient stock\",\"sku\":\"SKU-0001\"}", true));
    }

    @Test
    void stockFound() throws Exception {
        when(inventory.getStock("SKU-0001")).thenReturn(Optional.of(new StockLevel("SKU-0001", 5, true)));

        mvc.perform(get("/inventory/SKU-0001"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"sku\":\"SKU-0001\",\"stock\":5,\"cached\":true}", true));
    }

    @Test
    void stockUnknownIs404() throws Exception {
        when(inventory.getStock("NOPE")).thenReturn(Optional.empty());

        mvc.perform(get("/inventory/NOPE")).andExpect(status().isNotFound());
    }
}
