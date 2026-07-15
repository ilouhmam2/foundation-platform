package fr.francetv.foundation.core;

import fr.francetv.foundation.common.CorrelationIdUtils;
import fr.francetv.foundation.common.FoundationHeaders;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String MDC_KEY = "correlationId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String incomingHeader = request.getHeader(FoundationHeaders.CORRELATION_ID);
        String correlationId = (incomingHeader != null && !incomingHeader.isBlank())
                ? incomingHeader
                : CorrelationIdUtils.generate();

        MDC.put(MDC_KEY, correlationId);
        response.setHeader(FoundationHeaders.CORRELATION_ID, correlationId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}
