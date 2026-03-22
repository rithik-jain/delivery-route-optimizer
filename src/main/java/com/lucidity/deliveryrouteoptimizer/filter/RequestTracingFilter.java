package com.lucidity.deliveryrouteoptimizer.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Servlet filter that assigns a unique request ID to every incoming
 * HTTP request and stores it in SLF4J's MDC.
 *
 * <p>This means every log line produced while handling that request
 * will automatically include the request ID - without any class
 * needing to know about it or pass it around. Makes it trivial to
 * trace all log lines belonging to a single API call.</p>
 *
 * <p>The request ID is also returned in the response header
 * {@code X-Request-Id} so the caller can correlate their request
 * with server-side logs if needed.</p>
 *
 * @author Rithik Jain
 */
@Component
@Order(1)
public class RequestTracingFilter implements Filter {

    public static final String REQUEST_ID_KEY = "requestId";
    private static final String RESPONSE_HEADER = "X-Request-Id";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        String requestId = UUID.randomUUID().toString().substring(0, 8);

        try {
            MDC.put(REQUEST_ID_KEY, requestId);

            if (response instanceof HttpServletResponse httpResponse) {
                httpResponse.setHeader(RESPONSE_HEADER, requestId);
            }

            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}