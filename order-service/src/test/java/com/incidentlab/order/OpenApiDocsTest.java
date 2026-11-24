package com.incidentlab.order;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:openapi;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"})
@AutoConfigureMockMvc
class OpenApiDocsTest {

    @Autowired MockMvc mvc;

    @Test
    void serviceGroupDocumentsOrdersOnly() throws Exception {
        mvc.perform(get("/v3/api-docs/service"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("order-service"))
                .andExpect(jsonPath("$.paths['/orders'].post").exists())
                .andExpect(jsonPath("$.components.schemas.CreateOrderRequest").exists())
                .andExpect(jsonPath("$.paths['/admin/faults']").doesNotExist());
    }

    @Test
    void adminGroupDocumentsFaultEndpoints() throws Exception {
        mvc.perform(get("/v3/api-docs/admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/admin/faults'].get").exists())
                .andExpect(jsonPath("$.paths['/admin/faults/{id}/enable'].post").exists())
                .andExpect(jsonPath("$.paths['/admin/faults/{id}/disable'].post").exists());
    }

    @Test
    void actuatorGroupDocumentsHealth() throws Exception {
        mvc.perform(get("/v3/api-docs/actuator"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/actuator/health']").exists());
    }

    @Test
    void allGroupDocumentsEveryEndpoint() throws Exception {
        mvc.perform(get("/v3/api-docs/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/orders'].post").exists())
                .andExpect(jsonPath("$.paths['/admin/faults'].get").exists())
                .andExpect(jsonPath("$.paths['/actuator/health']").exists());
    }

    @Test
    void swaggerUiIsServedAndOpensOnAllEndpoints() throws Exception {
        mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
        mvc.perform(get("/v3/api-docs/swagger-config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.urls.length()").value(4))
                .andExpect(jsonPath("$['urls.primaryName']").value("All endpoints"));
    }
}
