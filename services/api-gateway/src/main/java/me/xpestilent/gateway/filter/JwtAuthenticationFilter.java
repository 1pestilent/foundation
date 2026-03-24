package me.xpestilent.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import me.xpestilent.gateway.security.JwtValidator;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Slf4j
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private final JwtValidator jwtValidator;

    public JwtAuthenticationFilter(JwtValidator jwtValidator) {
        super(Config.class);
        this.jwtValidator = jwtValidator;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "Отсутствует заголовок Authorization", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Неверный формат заголовка Authorization", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            return jwtValidator.validateAndParseToken(token)
                .flatMap(claims -> {
                    if (!"access".equals(claims.get("typ"))) {
                        return onError(exchange, "Ожидается  access-токен", HttpStatus.UNAUTHORIZED);
                    }

                    String userId = claims.getSubject();
                    List<String> rolesList = claims.get("roles", List.class);
                    String roles = rolesList != null ? String.join(",", rolesList) : "";

                    ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                        .headers(httpHeaders -> {
                            httpHeaders.remove("X-User-Id");
                            httpHeaders.remove("X-User-Roles");

                            httpHeaders.add("X-User-Id", userId);
                            httpHeaders.add("X-User-Roles", roles);
                        })
                        .build();

                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                })
                .onErrorResume(e -> {
                    log.warn("Ошибка валидации JWT на шлюзе: {}", e.getMessage());
                    return onError(exchange, "Невалидный или просроченный токен", HttpStatus.UNAUTHORIZED);
                });
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String errMessage, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);

        return response.setComplete();
    }

    public static class Config {
    }
}