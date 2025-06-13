package io.democratizedData.APIGateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final WebClient webClient;

    @Value("${services.auth-service.url}")
    private String authServiceUrl;

    @Value("${services.auth-service.validate-endpoint}")
    private String validateEndpoint;

    public AuthService(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<UserData> validateToken(String token) {
        logger.debug("Validating token with auth service: {}{}", authServiceUrl, validateEndpoint);

        return webClient.get()
                .uri(authServiceUrl + validateEndpoint)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .onStatus(HttpStatus.UNAUTHORIZED::equals,
                        response -> {
                            logger.warn("Auth service returned UNAUTHORIZED for token validation");
                            return Mono.error(new RuntimeException("Unauthorized: Invalid token"));
                        })
                .onStatus(HttpStatus.FORBIDDEN::equals,
                        response -> {
                            logger.warn("Auth service returned FORBIDDEN for token validation");
                            return Mono.error(new RuntimeException("Forbidden: Token expired"));
                        })
                .onStatus(status -> status.is4xxClientError(),
                        response -> {
                            logger.error("Auth service returned client error: {}", response.statusCode());
                            return Mono.error(new RuntimeException("Client error: " + response.statusCode()));
                        })
                .onStatus(status -> status.is5xxServerError(),
                        response -> {
                            logger.error("Auth service returned server error: {}", response.statusCode());
                            return Mono.error(new RuntimeException("Auth service error: " + response.statusCode()));
                        })
                .bodyToMono(Map.class)
                .map(userMap -> {
                    logger.debug("Successfully received user data from auth service");
                    return new UserData((Map<String, Object>) userMap);
                })
                .retryWhen(Retry.backoff(2, Duration.ofMillis(500))
                        .filter(throwable -> !(throwable instanceof WebClientResponseException.Unauthorized)))
                .onErrorMap(WebClientResponseException.class, ex -> {
                    logger.error("WebClient error during token validation: {} - {}",
                            ex.getStatusCode(), ex.getMessage());

                    if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                        return new RuntimeException("Invalid or expired token");
                    } else if (ex.getStatusCode() == HttpStatus.FORBIDDEN) {
                        return new RuntimeException("Token access forbidden");
                    } else {
                        return new RuntimeException("Auth service unavailable");
                    }
                })
                .onErrorMap(Exception.class, ex -> {
                    if (ex instanceof RuntimeException) {
                        return ex;
                    }
                    logger.error("Unexpected error during token validation", ex);
                    return new RuntimeException("Auth service communication failed");
                })
                .doOnSuccess(userData ->
                        logger.debug("Token validation successful for user: {}", userData.getUsername()))
                .doOnError(error ->
                        logger.error("Token validation failed: {}", error.getMessage()));
    }
}