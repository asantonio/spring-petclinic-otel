package org.springframework.samples.petclinic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

import io.opentelemetry.api.trace.Span;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

@SpringBootApplication
public class PetClinicApplication {

	public static void main(String[] args) {
		SpringApplication.run(PetClinicApplication.class, args);
	}

	@Bean
	public FilterRegistrationBean<CustomHeaderFilter> customHeaderFilter() {
		FilterRegistrationBean<CustomHeaderFilter> registrationBean = new FilterRegistrationBean<>();
		registrationBean.setFilter(new CustomHeaderFilter());
		registrationBean.addUrlPatterns("/*");
		return registrationBean;
	}

	public static class CustomHeaderFilter implements Filter {

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

}
