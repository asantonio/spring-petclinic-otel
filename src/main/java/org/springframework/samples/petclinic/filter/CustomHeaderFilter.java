package org.springframework.samples.petclinic.filter;

import io.opentelemetry.api.trace.Span;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

public class CustomHeaderFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            String headerValue = httpRequest.getHeader("x-splunk-test");

            if (headerValue != null) {
                parseCustomHeader(headerValue);
            }
        }
        chain.doFilter(request, response);
    }

    public void parseCustomHeader(String headerValue) {
        String[] headerTokens = headerValue.split(";");
        Span currentSpan = Span.current();
        for (String token : headerTokens) {
            String[] keyValue = token.split("=");
            if (keyValue.length == 2) {
                currentSpan.setAttribute(keyValue[0], keyValue[1]);
            }
        }
    }

}
