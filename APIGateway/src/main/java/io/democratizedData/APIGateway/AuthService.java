package io.democratizedData.APIGateway;

import com.democratizeddata.gateway.dto.UserData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class AuthService {

    private final WebClient webClient;

    @Value("${services.auth-service.url}")
    private String authServiceUrl;

    @Value("${services.auth-service.validate-endpoint}")
    private String validateEndpoint;

    public AuthService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public Mono<UserData> validateToken(String token) {
        return webClient.get()
                .uri(authServiceUrl + validateEndpoint)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .onStatus(HttpStatus.UNAUTHORIZED::equals,
                        response -> Mono.error(new RuntimeException("Unauthorized: Invalid token")))
                .onStatus(HttpStatus.FORBIDDEN::equals,
                        response -> Mono.error(new RuntimeException("Forbidden: Token expired")))
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> Mono.error(new RuntimeException("Auth service error: " + response.statusCode())))
                .bodyToMono(Map.class)  // Changed from UserData.class to Map.class
                .map(userMap -> new UserData((Map<String, Object>) userMap))  // Convert Map to UserData
                .onErrorMap(WebClientResponseException.class, ex -> {
                    if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                        return new RuntimeException("Invalid or expired token");
                    }
                    return new RuntimeException("Auth service unavailable");
                });
    }
}