package com.lockerline;

import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.lockerline.error.CodeExpired;
import com.lockerline.error.NoSuchCode;
import com.lockerline.pickup.PickupController;
import com.lockerline.pickup.PickupService;

/**
 * Only the web layer - the pickup controller and the one exception handler - with the service replaced by a
 * mock. It proves each refusal the service throws is mapped to its status and type, without a database.
 */
@WebMvcTest(PickupController.class)
class PickupControllerSliceTest {

    @Autowired
    MockMvc http;

    @MockitoBean
    PickupService pickups;

    @Test
    void noSuchCodeIsMappedTo404() throws Exception {
        doThrow(new NoSuchCode()).when(pickups).pickup("A", "111111");
        http.perform(post("/pickups").contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"111111\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("/errors/no-such-code"));
    }

    @Test
    void codeExpiredIsMappedTo404WithItsOwnType() throws Exception {
        doThrow(new CodeExpired()).when(pickups).pickup("A", "222222");
        http.perform(post("/pickups").contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"222222\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("/errors/code-expired"));
    }
}
