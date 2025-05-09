package org.eclipse.ecsp.threadlocal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PlatformThreadLocalTest {

    private static final String PLATFORM_ID = "test-platform";

    @AfterEach
    void tearDown() {
        PlatformThreadLocal.clear();
    }

    @Test
    void testSetAndGetPlatformId() {
        PlatformThreadLocal.setPlatformId(PLATFORM_ID);

        assertEquals(PLATFORM_ID, PlatformThreadLocal.getPlatformId());
    }

    @Test
    void testClearPlatformId() {
        PlatformThreadLocal.setPlatformId(PLATFORM_ID);
        PlatformThreadLocal.clear();

        assertNull(PlatformThreadLocal.getPlatformId());
    }

    @Test
    void testGetPlatformIdWhenNotSet() {
        assertNull(PlatformThreadLocal.getPlatformId());
    }
}
