package com.incidentlab.inventory.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import com.incidentlab.inventory.entity.Product;
import com.incidentlab.inventory.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean ProductService products;

    @Test
    void returnsNameAndPrice() throws Exception {
        when(products.getProduct("SKU-0001")).thenReturn(Optional.of(new Product("SKU-0001", "Product 1", 1999, 10)));

        mvc.perform(get("/products/SKU-0001"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"sku\":\"SKU-0001\",\"name\":\"Product 1\",\"priceCents\":1999}", true));
    }

    @Test
    void unknownSkuIs404() throws Exception {
        when(products.getProduct("NOPE")).thenReturn(Optional.empty());

        mvc.perform(get("/products/NOPE")).andExpect(status().isNotFound());
    }
}
