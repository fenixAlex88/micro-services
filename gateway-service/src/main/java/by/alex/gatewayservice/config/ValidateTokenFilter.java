package by.alex.gatewayservice.config;

import by.alex.gatewayservice.client.AuthServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Component
public class ValidateTokenFilter extends AbstractGatewayFilterFactory<ValidateTokenFilter.Config> {

    private final AuthServiceClient authServiceClient;

    public ValidateTokenFilter(AuthServiceClient authServiceClient) {
        super(Config.class);
        this.authServiceClient = authServiceClient;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().toString();

            log.info("Request path: {}", path);

            // Пропускаем исключенные пути
            if (config.getExcludePaths() != null && path.startsWith(config.getExcludePaths())) {
                log.info("Excluding path: {}", path);
                return chain.filter(exchange);
            }

            String token = extractToken(request);

            if (token == null) {
                log.warn("Token not found");
                return handleUnauthorized(exchange, HttpStatus.FORBIDDEN);
            }

            return authServiceClient.validateToken(token)
                    .flatMap(claims -> {
                        log.info("Token validated, claims: {}", claims);
                        // Модифицируем запрос, добавляя данные из токена в заголовки
                        ServerHttpRequest modifiedRequest = addClaimsToHeaders(request, claims);
                        // Передаем модифицированный запрос в цепочку фильтров
                        return chain.filter(exchange.mutate().request(modifiedRequest).build());
                    })
                    .onErrorResume(e -> {
                        log.warn("Token validation failed: {}", e.getMessage());
                        return handleUnauthorized(exchange, HttpStatus.FORBIDDEN);
                    });
        };
    }

    private String extractToken(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private ServerHttpRequest addClaimsToHeaders(ServerHttpRequest request, Map<String, Object> claims) {
        ServerHttpRequest.Builder modifiedRequest = request.mutate();
        for (String claimKey : claims.keySet()) {
            modifiedRequest.header("X-Auth-"+claimKey, claims.get(claimKey).toString());
        }
        return modifiedRequest.build();
    }

    private Mono<Void> handleUnauthorized(ServerWebExchange exchange, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        return response.setComplete();
    }

    public static class Config {
        private String excludePaths;

        public String getExcludePaths() {
            return excludePaths;
        }

        public void setExcludePaths(String excludePaths) {
            this.excludePaths = excludePaths;
        }
    }
}