package io.democratizedData.APIGateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilter.Config> {

    private static final Logger logger = LoggerFactory.getLogger(AuthFilter.class);

    @Autowired
    private AuthService authService;

    public AuthFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            logger.debug("Processing request to path: {}", path);

            // Check if Authorization header exists
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                logger.warn("Authorization header missing for path: {}", path);
                return onError(exchange, "Authorization header is missing", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            // Check if it's a Bearer token
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.warn("Invalid authorization header format for path: {}", path);
                return onError(exchange, "Authorization header must be Bearer token", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7); // Remove "Bearer " prefix
            logger.debug("Validating token for path: {}", path);

            return authService.validateToken(token)
                    .flatMap(userData -> {
                        logger.debug("Token validated successfully for user: {} on path: {}",
                                userData.getUsername(), path);

                        ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                                .header("X-User-Id", String.valueOf(userData.getUserId()))
                                .header("X-User-Username", userData.getUsername())
                                .header("X-User-Role", userData.getRole())
                                .header("X-User-City", userData.getCity() != null ? userData.getCity() : "")
                                .header("X-User-Gender", userData.getGender() != null ? userData.getGender() : "")
                                .build();

                        return chain.filter(exchange.mutate().request(modifiedRequest).build());
                    })
                    .onErrorResume(error -> {
                        logger.error("Token validation failed for path: {} - Error: {}", path, error.getMessage());
                        return onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
                    });
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String error, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        String body = String.format("{\"error\":\"%s\",\"status\":%d,\"timestamp\":\"%s\"}",
                error, httpStatus.value(), java.time.Instant.now());

        org.springframework.core.io.buffer.DataBuffer buffer = response.bufferFactory().wrap(body.getBytes());

        return response.writeWith(Mono.just(buffer));
    }

    public static class Config {
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
