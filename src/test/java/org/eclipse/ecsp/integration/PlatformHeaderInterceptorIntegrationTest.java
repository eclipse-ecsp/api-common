package org.eclipse.ecsp.integration;

import io.prometheus.client.CollectorRegistry;
import org.eclipse.ecsp.testutils.CommonTestBase;
import org.eclipse.ecsp.threadlocal.PlatformThreadLocal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "base.package=com.example.test"
})
@DirtiesContext
class PlatformHeaderInterceptorIntegrationTest extends CommonTestBase {
    private static final String PLATFORM_ID = "platform-id";
    @Autowired
    private MockMvc mockMvc; // Use MockMvc for testing

    static {
        CollectorRegistry.defaultRegistry.clear();
    }

    @BeforeEach
    void setUp() {
        // Clear the CollectorRegistry to avoid duplicate metrics registration
        CollectorRegistry.defaultRegistry.clear();
    }

    @AfterEach
    void tearDown() {
        PlatformThreadLocal.clear();
    }

    @Test
    void testInterceptorSetsPlatformId() throws Exception {
        String platformId = "test-platform-id";

        mockMvc.perform(get("/test/platform-id")
                .header(PLATFORM_ID, platformId))
                .andExpect(status().isOk())
                .andExpect(content().string(platformId));
        assertNull(PlatformThreadLocal.getPlatformId(), "Platform ID should be cleared after request");

    }

    @Test
    void testInterceptorNoPlatformId() throws Exception {
        mockMvc.perform(get("/test/platform-id"))
                .andExpect(status().isOk())
                .andExpect(content().string("No Platform ID found"));

        assertNull(PlatformThreadLocal.getPlatformId(), "Platform ID should be cleared after request");
    }
}
