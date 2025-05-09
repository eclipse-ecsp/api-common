package org.eclipse.ecsp.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.ecsp.threadlocal.PlatformThreadLocal;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * PlatformHeaderInterceptor to intercept HTTP requests and store the platformId header value in a ThreadLocal variable.
 */
@Component
public class PlatformHeaderInterceptor implements HandlerInterceptor {
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(PlatformHeaderInterceptor.class);
    @Value("${platform.header.name:platform-id}")
    private String headerName;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        String headerValue = request.getHeader(headerName);
        if (headerValue != null) {
            LOGGER.debug("Setting platformId: {} for request: {}", headerValue, request.getRequestURI());
            PlatformThreadLocal.setPlatformId(headerValue);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        LOGGER.debug("Clearing platformId for request: {}", request.getRequestURI());
        PlatformThreadLocal.clear();
    }
}
