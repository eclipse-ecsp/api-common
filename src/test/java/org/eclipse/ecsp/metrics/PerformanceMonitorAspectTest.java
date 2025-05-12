package org.eclipse.ecsp.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.example.test.ExampleRestController;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Histogram;
import io.prometheus.client.hotspot.DefaultExports;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.HandlerMapping;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PerformanceMonitorAspectTest {
    public static final double DOUBLE_POINT_5 = 0.05;
    public static final double DOUBLE_POINT_1 = 0.1;
    public static final double DOUBLE_POINT_2 = 0.2;

    static {
        CollectorRegistry.defaultRegistry.clear();
    }

    @Mock
    private MetricRegistry registry;

    @Mock
    private HttpServletRequest request;

    @Mock
    private ProceedingJoinPoint joinPoint;

    private PerformanceMonitorAspect aspect;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        aspect = new PerformanceMonitorAspect(registry, request);
        // Use ReflectionTestUtils to set private fields
        ReflectionTestUtils.setField(aspect, "newAgeMetricsEnabled", true);
        ReflectionTestUtils.setField(aspect, "legacyPerformanceMonitoringEnabled", true);
        ReflectionTestUtils.setField(aspect, "apiProcessingDurationBuckets",
                new double[]{DOUBLE_POINT_5, DOUBLE_POINT_1, DOUBLE_POINT_2});
        ReflectionTestUtils.setField(aspect, "nodeName", "test-node");
    }

    @Test
    void testInitWithNewAgeMetricsEnabled() {
        ReflectionTestUtils.setField(aspect, "newAgeMetricsEnabled", true);

        try (var mockedStatic = mockStatic(DefaultExports.class)) {
            aspect.init();

            // Use ReflectionTestUtils to get the private field value
            Histogram latencyHisto = (Histogram) ReflectionTestUtils.getField(aspect, "latencyHisto");
            assertNotNull(latencyHisto);
            mockedStatic.verify(DefaultExports::initialize);
        }
    }

    @Test
    void testInitWithNewAgeMetricsDisabled() {
        ReflectionTestUtils.setField(aspect, "newAgeMetricsEnabled", false);

        aspect.init();

        // Use ReflectionTestUtils to get the private field value
        Histogram latencyHisto = (Histogram) ReflectionTestUtils.getField(aspect, "latencyHisto");
        assertNull(latencyHisto);
    }

    @Test
    void testMonitorWithNewAgeMetricsEnabledAndRestController() throws Throwable {
        ReflectionTestUtils.setField(aspect, "newAgeMetricsEnabled", true);
        // Mock the Signature object
        org.aspectj.lang.Signature signature = mock(org.aspectj.lang.Signature.class);
        doReturn("ExampleRestController").when(signature).getDeclaringTypeName();
        when(signature.getDeclaringType()).thenReturn(ExampleRestController.class);
        when(joinPoint.getSignature()).thenReturn(signature);

        // Mock request attributes
        when(request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE)).thenReturn("/test/api");
        when(request.getMethod()).thenReturn("GET");

        // Mock Histogram and Timer
        Timer timer = mock(Timer.class);
        Histogram histogram = mock(Histogram.class);
        when(registry.timer(anyString())).thenReturn(timer);

        Histogram.Child histogramChild = mock(Histogram.Child.class);
        when(histogram.labels(anyString(), anyString(), anyString())).thenReturn(histogramChild);
        when(histogramChild.startTimer()).thenReturn(mock(Histogram.Timer.class));

        // Use ReflectionTestUtils to set the private field value
        ReflectionTestUtils.setField(aspect, "latencyHisto", histogram);

        // Mock the joinPoint.proceed() method
        Object result = new Object();
        when(joinPoint.proceed()).thenReturn(result);



        // Execute the method under test
        Object actualResult = aspect.monitor(joinPoint);

        // Verify the ThreadLocal behavior
        ThreadLocal<String> currentApiValue = (ThreadLocal<String>) ReflectionTestUtils.getField(aspect, "currentApi");
        assertNull(currentApiValue.get(), "ThreadLocal value should be cleared");

        // Verify the results
        assertEquals(result, actualResult);
    }

    @Test
    void testMonitorWithLegacyPerformanceMonitoringEnabled() throws Throwable {
        org.aspectj.lang.Signature signature = mock(org.aspectj.lang.Signature.class);
        when(signature.getDeclaringType()).thenReturn(ExampleRestController.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        doReturn("TestController").when(signature).getDeclaringTypeName();
        when(signature.getDeclaringType()).thenReturn(Object.class);
        when(request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE)).thenReturn("/test/api");
        when(request.getMethod()).thenReturn("GET");
        when(joinPoint.getSignature().toLongString()).thenReturn("testMethod");

        // Mock the Timer and its Context
        Timer timer = mock(Timer.class);
        Timer.Context context = mock(Timer.Context.class);
        when(timer.time()).thenReturn(context);

        // Ensure registry.timer() returns the mocked Timer
        when(registry.timer(anyString())).thenReturn(timer);

        Object result = new Object();
        when(joinPoint.proceed()).thenReturn(result);

        Object actualResult = aspect.monitor(joinPoint);

        assertEquals(result, actualResult);
        verify(context).stop();
    }

    @Test
    void testMonitorWithNoMonitoringEnabled() throws Throwable {
        ReflectionTestUtils.setField(aspect, "newAgeMetricsEnabled", false);
        ReflectionTestUtils.setField(aspect, "legacyPerformanceMonitoringEnabled", false);

        Object result = new Object();
        when(joinPoint.proceed()).thenReturn(result);

        Object actualResult = aspect.monitor(joinPoint);

        assertEquals(result, actualResult);
    }

    @Test
    void testMonitorWithNullApi() throws Throwable {
        ReflectionTestUtils.setField(aspect, "newAgeMetricsEnabled", true);
        // Mock the Signature object
        org.aspectj.lang.Signature signature = mock(org.aspectj.lang.Signature.class);
        doReturn("ExampleRestController").when(signature).getDeclaringTypeName();
        when(signature.getDeclaringType()).thenReturn(ExampleRestController.class);
        when(joinPoint.getSignature()).thenReturn(signature);

        // Mock request attributes
        when(request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE)).thenReturn(null);
        when(request.getMethod()).thenReturn("GET");
        // Mock the request URL
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://example.com/test/api"));

        // Mock Histogram and Timer
        Timer timer = mock(Timer.class);
        Histogram histogram = mock(Histogram.class);
        when(registry.timer(anyString())).thenReturn(timer);

        Histogram.Child histogramChild = mock(Histogram.Child.class);
        when(histogram.labels(anyString(), anyString(), anyString())).thenReturn(histogramChild);
        when(histogramChild.startTimer()).thenReturn(mock(Histogram.Timer.class));

        // Use ReflectionTestUtils to set the private field value
        ReflectionTestUtils.setField(aspect, "latencyHisto", histogram);

        // Mock the joinPoint.proceed() method
        Object result = new Object();
        when(joinPoint.proceed()).thenReturn(result);



        // Execute the method under test
        Object actualResult = aspect.monitor(joinPoint);

        // Verify the ThreadLocal behavior
        ThreadLocal<String> currentApiValue = (ThreadLocal<String>) ReflectionTestUtils.getField(aspect, "currentApi");
        assertNull(currentApiValue.get(), "ThreadLocal value should be cleared");

        // Verify the results
        assertEquals(result, actualResult);
    }

    @Test
    void testAnonymizeUrl() {
        String url = "http://example.com/users/123/vehicles/456/test";
        String anonymized = (String) ReflectionTestUtils.invokeMethod(aspect, "anonymizeUrl", url);
        assertEquals("http://example.com/users/{uid}/vehicles/{vid}/test", anonymized);
    }
}
