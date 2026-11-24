package com.incidentlab.inventory.service;

import java.util.Optional;

import com.incidentlab.inventory.entity.Product;
import com.incidentlab.inventory.repository.ProductRepository;
import org.springframework.stereotype.Service;

/** Product catalog: name and current list price. */
@Service
public class ProductService {

    private final ProductRepository products;

    public ProductService(ProductRepository products) {
        this.products = products;
    }

    public Optional<Product> getProduct(String sku) {
        return products.findById(sku);
    }
}
