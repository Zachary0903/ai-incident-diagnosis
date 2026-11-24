package com.incidentlab.order.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.incidentlab.order.dto.ProductResponse;
import com.incidentlab.order.exception.UnknownProductException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

class InventoryClientTest {

    private final RestClient.Builder builder = RestClient.builder().baseUrl("http://inventory");
    private final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    private final InventoryClient client = new InventoryClient(builder.build());

    @Test
    void getProductReadsCatalog() {
        server.expect(requestTo("http://inventory/products/SKU-0001"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"sku":"SKU-0001","name":"Product 1","priceCents":1999}
                        """, MediaType.APPLICATION_JSON));

        assertThat(client.getProduct("SKU-0001")).isEqualTo(new ProductResponse("SKU-0001", "Product 1", 1999));
        server.verify();
    }

    @Test
    void getProductNotFoundBecomesUnknownProduct() {
        server.expect(requestTo("http://inventory/products/NOPE")).andRespond(withResourceNotFound());

        assertThatThrownBy(() -> client.getProduct("NOPE"))
                .isInstanceOf(UnknownProductException.class);
    }

    @Test
    void getProductOtherErrorsStayRestClientErrors() {
        server.expect(requestTo("http://inventory/products/SKU-0001")).andRespond(withServerError());

        assertThatThrownBy(() -> client.getProduct("SKU-0001"))
                .isInstanceOf(RestClientResponseException.class);
    }

    @Test
    void reserveStockPostsSkuAndQty() {
        server.expect(requestTo("http://inventory/inventory/reserve"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("""
                        {"sku":"SKU-0001","qty":2}
                        """, true))
                .andRespond(withSuccess());

        client.reserveStock("SKU-0001", 2);
        server.verify();
    }

    @Test
    void reserveStockConflictPropagates() {
        server.expect(requestTo("http://inventory/inventory/reserve")).andRespond(withStatus(HttpStatus.CONFLICT));

        assertThatThrownBy(() -> client.reserveStock("SKU-0001", 2))
                .isInstanceOf(RestClientResponseException.class);
    }
}
