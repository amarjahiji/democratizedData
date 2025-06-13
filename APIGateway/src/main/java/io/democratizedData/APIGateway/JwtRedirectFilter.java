package io.democratizedData.APIGateway;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

// Disabled to avoid conflicts with AuthFilter
// @Component
public class JwtRedirectFilter implements GlobalFilter, Ordered {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private static final String BEARER = "Bearer ";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // Always allow login/register through so user can sign in
        if (path.startsWith("/auth/")) {
            return chain.filter(exchange);
        }

        String auth = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        // 1) If no token → redirect client to /auth/signin
        if (auth == null || !auth.startsWith(BEARER)) {
            exchange.getResponse().setStatusCode(HttpStatus.TEMPORARY_REDIRECT);
            exchange.getResponse().getHeaders().setLocation(URI.create("/auth/signin"));
            return exchange.getResponse().setComplete();
        }

        // 2) If token present, verify it
        String token = auth.substring(BEARER.length());
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(jwtSecret)).build();
            DecodedJWT jwt = verifier.verify(token);

            // 3) Extract role & subject for downstream
            String role   = jwt.getClaim("role").asString();
            String userId = jwt.getSubject();

            ServerHttpRequest mutated = exchange.getRequest().mutate()
                    .header("X-User-Id", userId)
                    .header("X-User-Role", role)
                    .build();

            return chain.filter(exchange.mutate().request(mutated).build());

        } catch (JWTVerificationException ex) {
            // Invalid or expired → redirect to sign in again
            exchange.getResponse().setStatusCode(HttpStatus.TEMPORARY_REDIRECT);
            exchange.getResponse().getHeaders().setLocation(URI.create("/auth/signin"));
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
