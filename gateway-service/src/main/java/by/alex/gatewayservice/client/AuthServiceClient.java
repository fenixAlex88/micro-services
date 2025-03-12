package by.alex.gatewayservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Component
public class AuthServiceClient {

    private final WebClient webClient;

    public AuthServiceClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8082").build();
    }

    public Mono<Map<String, Object>> validateToken(String token) {
        log.info("Validating token: {}", token);

        return webClient.post()
                .uri("/auth/validate")
                .bodyValue(token)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .flatMap(response -> {
                    if (response == null || response.isEmpty()) {
                        log.warn("Token is invalid or empty response");
                        return Mono.error(new RuntimeException("Invalid token"));
                    }
                    return Mono.just(response);
                })
                .onErrorResume(e -> {
                    log.error("Error occurred while validating token", e);
                    return Mono.error(e); // Пробрасываем ошибку дальше
                });
    }
}