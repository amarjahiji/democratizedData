package io.democratizedData.APIGateway;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class AuthenticationFilter implements GatewayFilter {

    @Autowired
    private WebClient webClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Extract Authorization header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return handleUnauthorized(exchange);
        }

        String token = authHeader.substring(7);

        // Validate token with auth service
        return validateToken(token)
                .flatMap(userInfo -> {
                    // Modify request body to include user info
                    return modifyRequestBody(exchange, userInfo)
                            .flatMap(chain::filter);
                })
                .onErrorResume(throwable -> handleUnauthorized(exchange));
    }

    private Mono<UserInfo> validateToken(String token) {
        TokenValidationRequest validationRequest = new TokenValidationRequest(token);

        return webClient.post()
                .uri("http://localhost:8081/auth/validate-token")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(validationRequest)
                .retrieve()
                .bodyToMono(UserInfo.class);
    }

    private Mono<ServerWebExchange> modifyRequestBody(ServerWebExchange exchange, UserInfo userInfo) {
        ServerHttpRequest request = exchange.getRequest();
        DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();

        return DataBufferUtils.join(request.getBody())
                .defaultIfEmpty(bufferFactory.allocateBuffer(0))
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    return new String(bytes, StandardCharsets.UTF_8);
                })
                .map(originalBody -> {
                    try {
                        JsonNode originalJson;
                        if (originalBody.isEmpty()) {
                            originalJson = objectMapper.createObjectNode();
                        } else {
                            originalJson = objectMapper.readTree(originalBody);
                        }

                        // Add user info to the request body
                        ((com.fasterxml.jackson.databind.node.ObjectNode) originalJson)
                                .set("user", objectMapper.valueToTree(userInfo));

                        return objectMapper.writeValueAsString(originalJson);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("Failed to modify request body", e);
                    }
                })
                .map(modifiedBody -> {
                    byte[] bytes = modifiedBody.getBytes(StandardCharsets.UTF_8);
                    DataBuffer buffer = bufferFactory.wrap(bytes);

                    ServerHttpRequest mutatedRequest = new ServerHttpRequestDecorator(request) {
                        @Override
                        public Flux<DataBuffer> getBody() {
                            return Flux.just(buffer);
                        }

                        @Override
                        public HttpHeaders getHeaders() {
                            HttpHeaders headers = new HttpHeaders();
                            headers.putAll(super.getHeaders());
                            headers.setContentLength(bytes.length);
                            headers.setContentType(MediaType.APPLICATION_JSON);
                            return headers;
                        }
                    };

                    return exchange.mutate().request(mutatedRequest).build();
                });
    }

    private Mono<Void> handleUnauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        String errorMessage = "{\"error\": \"Unauthorized\", \"message\": \"Valid JWT token required\"}";
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(errorMessage.getBytes());

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}