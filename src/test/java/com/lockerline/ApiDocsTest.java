package com.lockerline;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

import com.lockerline.parcel.ParcelService;

/** The API docs another team builds against, and the one shape every error has - a 500 included. */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:api-docs-test")
@AutoConfigureMockMvc
class ApiDocsTest {

    @Autowired
    MockMvc http;

    @MockitoSpyBean
    ParcelService parcels;
  
    @Test
    void theSpecDocumentsEveryEndpointAndItsRefusals() throws Exception {
        http.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/drops'].post.summary").isNotEmpty())
                .andExpect(jsonPath("$.paths['/drops'].post.responses['409']").exists())
                .andExpect(jsonPath("$.paths['/pickups'].post.responses['404']").exists())
                .andExpect(jsonPath("$.paths['/parcels/{parcelId}'].get.tags[0]").value("parcels"))
                .andExpect(jsonPath("$.paths['/towers/{tower}/doors'].get.responses['400']").exists());
    }

    @Test
    void anUnexpectedErrorIsAGeneric500WithARequestId() throws Exception {
        doThrow(new IllegalStateException("database on fire"))
                .when(parcels)
                .parcel(anyString());

        http.perform(get("/parcels/P001"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.type").value("/errors/internal"))
                .andExpect(jsonPath("$.requestId").isNotEmpty())
                .andExpect(content().string(
                        Matchers.not(Matchers.containsString("on fire"))));
    }

}
