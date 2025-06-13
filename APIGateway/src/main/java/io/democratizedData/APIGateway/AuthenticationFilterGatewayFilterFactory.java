package io.democratizedData.APIGateway;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

// Disabled to avoid conflicts with AuthFilter
// @Component
public class AuthenticationFilterGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthenticationFilterGatewayFilterFactory.Config> {

    public AuthenticationFilterGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // Your authentication logic here
            // For example, check for tokens in headers, validate them, etc.

            // For demonstration - check if Authorization header exists
            if (!exchange.getRequest().getHeaders().containsKey("Authorization")) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authorization header");
            }

            return chain.filter(exchange);
        };
    }

    public static class Config {
        // Add configuration properties if needed
        private String headerName = "Authorization";

        public String getHeaderName() {
            return headerName;
        }

        public void setHeaderName(String headerName) {
            this.headerName = headerName;
        }
    }
}
