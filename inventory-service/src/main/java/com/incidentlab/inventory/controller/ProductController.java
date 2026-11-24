package com.incidentlab.inventory.controller;

import com.incidentlab.inventory.dto.ProductResponse;
import com.incidentlab.inventory.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/** Product catalog endpoints. order-service prices orders from {@code GET /products/{sku}}. */
@RestController
public class ProductController {

    private final ProductService products;

    public ProductController(ProductService products) {
        this.products = products;
    }

    @GetMapping("/products/{sku}")
    public ResponseEntity<ProductResponse> product(@PathVariable String sku) {
        return products.getProduct(sku)
                .map(p -> ResponseEntity.ok(new ProductResponse(p.getSku(), p.getName(), p.getPriceCents())))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
