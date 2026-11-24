package com.incidentlab.inventory.controller;

import com.incidentlab.inventory.dto.ErrorResponse;
import com.incidentlab.inventory.dto.ReservationResponse;
import com.incidentlab.inventory.dto.ReserveRequest;
import com.incidentlab.inventory.dto.StockResponse;
import com.incidentlab.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InventoryController {

    private final InventoryService inventory;

    public InventoryController(InventoryService inventory) {
        this.inventory = inventory;
    }

    @GetMapping("/inventory/{sku}")
    public ResponseEntity<StockResponse> stock(@PathVariable String sku) {
        return inventory.getStock(sku)
                .map(level -> ResponseEntity.ok(new StockResponse(level.sku(), level.stock(), level.cached())))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/inventory/reserve")
    @ApiResponse(responseCode = "200", description = "Stock reserved",
            content = @Content(schema = @Schema(implementation = ReservationResponse.class)))
    @ApiResponse(responseCode = "409", description = "Unknown SKU or insufficient stock",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<?> reserve(@RequestBody ReserveRequest request) {
        return inventory.reserve(request.sku(), request.qty())
                .<ResponseEntity<?>>map(r -> ResponseEntity.ok(new ReservationResponse(r.sku(), r.qty())))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new ErrorResponse("insufficient stock", request.sku())));
    }
}
