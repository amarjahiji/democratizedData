package io.democratizedData.APIGateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()

                // 1) Auth endpoints: no JWT required
                .route("auth", r -> r.path("/auth/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("http://localhost:8081"))

                // 2) User endpoints: JWT is validated globally
                .route("user", r -> r.path("/users/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("http://localhost:8082"))

                // 3) Admin endpoints: JWT validated globally + role check
                .route("admin", r -> r.path("/admin/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .filter(new RoleAuthorizationFilter("ADMIN")))
                        .uri("http://localhost:8083"))

                .build();
    }
}