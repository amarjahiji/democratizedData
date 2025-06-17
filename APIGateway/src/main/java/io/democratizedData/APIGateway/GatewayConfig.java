package io.democratizedData.APIGateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class GatewayConfig {

    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }

    @Bean
    public RouteLocator customRoutes(RouteLocatorBuilder builder, AuthenticationFilter authFilter) {
        return builder.routes()
                // Public routes - no authentication required
                .route("auth-register", r -> r.path("/auth/register")
                        .uri("http://localhost:8081"))
                .route("auth-login", r -> r.path("/auth/login")
                        .uri("http://localhost:8081"))
                .route("polls-active", r -> r.path("/api/polls/active")
                        .uri("http://localhost:8082"))

                // Protected routes - authentication required
                .route("polls-create", r -> r.path("/api/polls/create")
                        .filters(f -> f.filter(authFilter))
                        .uri("http://localhost:8082"))

                .route("analytics-votes-over-time", r -> r.path("/analytics/poll/{pollId}/votes-over-time")
                        .filters(f -> f.filter(authFilter))
                        .uri("http://localhost:8083"))

                .route("analytics-percentage-distribution", r -> r.path("/analytics/poll/{pollId}/percentage-distribution")
                        .filters(f -> f.filter(authFilter))
                        .uri("http://localhost:8083"))

                .route("analytics-demographics", r -> r.path("/analytics/poll/{pollId}/demographics")
                        .filters(f -> f.filter(authFilter))
                        .uri("http://localhost:8083"))

                .route("analytics-export", r -> r.path("/analytics/poll/{pollId}/votes-over-time/export")
                        .filters(f -> f.filter(authFilter))
                        .uri("http://localhost:8083"))

                .route("poll-save", r -> r.path("/poll/save")
                        .filters(f -> f.filter(authFilter))
                        .uri("http://localhost:8083"))

                .route("poll-get", r -> r.path("/poll/{poll_id}/get")
                        .filters(f -> f.filter(authFilter))
                        .uri("http://localhost:8083"))

                .route("poll-votes", r -> r.path("/poll/votes")
                        .filters(f -> f.filter(authFilter))
                        .uri("http://localhost:8083"))

                .route("poll-votes-by-id", r -> r.path("/poll/{poll_id}/votes")
                        .filters(f -> f.filter(authFilter))
                        .uri("http://localhost:8083"))

                .route("poll-vote-option", r -> r.path("/poll/{poll_id}/votes/{option}")
                        .filters(f -> f.filter(authFilter))
                        .uri("http://localhost:8083"))

                .build();
    }
}