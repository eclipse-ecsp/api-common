package org.eclipse.ecsp.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.ecsp.threadlocal.PlatformThreadLocal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

class PlatformHeaderInterceptorTest {

    private static final String HEADER_NAME = "platform-id";
    private static final String HEADER_VALUE = "test-platform";

    private PlatformHeaderInterceptor interceptor;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        interceptor = new PlatformHeaderInterceptor();
        ReflectionTestUtils.setField(interceptor, "headerName", HEADER_NAME);
    }

    @AfterEach
    void tearDown() {
        PlatformThreadLocal.clear();
    }

    @Test
    void testPreHandleSetsPlatformId() {
        when(request.getHeader(HEADER_NAME)).thenReturn(HEADER_VALUE);

        boolean result = interceptor.preHandle(request, response, null);

        assertEquals(true, result);
        assertEquals(HEADER_VALUE, PlatformThreadLocal.getPlatformId());
    }

    @Test
    void testPreHandleDoesNotSetPlatformIdWhenHeaderIsMissing() {
        when(request.getHeader(HEADER_NAME)).thenReturn(null);

        boolean result = interceptor.preHandle(request, response, null);

        assertEquals(true, result);
        assertNull(PlatformThreadLocal.getPlatformId());
    }

    @Test
    void testAfterCompletionClearsPlatformId() {
        PlatformThreadLocal.setPlatformId(HEADER_VALUE);

        interceptor.afterCompletion(request, response, null, null);

        assertNull(PlatformThreadLocal.getPlatformId());
    }

    @Test
    void testThreadLocalIsClearedAfterCompletion() {
        // Set a value in the ThreadLocal
        PlatformThreadLocal.setPlatformId(HEADER_VALUE);

        // Call afterCompletion to clear the ThreadLocal
        interceptor.afterCompletion(request, response, null, null);

        // Assert that the ThreadLocal is cleared
        assertNull(PlatformThreadLocal.getPlatformId(), "ThreadLocal should be cleared after afterCompletion");
    }
}
