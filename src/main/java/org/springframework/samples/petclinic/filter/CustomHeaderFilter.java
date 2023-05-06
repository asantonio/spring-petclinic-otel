package org.springframework.samples.petclinic.filter;

import io.opentelemetry.api.trace.Span;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CustomHeaderFilter implements Filter {

    private final List<String> headerNames;

    public CustomHeaderFilter() {
        headerNames = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("headerCapture.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                headerNames.add(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            for (String headerName : headerNames) {
                String headerValue = httpRequest.getHeader(headerName);
                if (headerValue != null) {
                    parseCustomHeader(headerValue);
                }
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
