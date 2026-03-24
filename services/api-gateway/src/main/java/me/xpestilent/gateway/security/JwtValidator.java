package me.xpestilent.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
@Slf4j
public class JwtValidator {

    private PublicKey publicKey;
    private final WebClient webClient;

    public JwtValidator(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8081").build();
    }

    private Mono<PublicKey> getPublicKey() {
        if (this.publicKey != null) {
            return Mono.just(this.publicKey);
        }

        log.info("Fetching public key from auth-service...");
        return webClient.get()
            .uri("/api/v1/auth/public-key")
            .retrieve()
            .bodyToMono(String.class)
            .map(this::parsePublicKey)
            .doOnNext(key -> {
                this.publicKey = key;
                log.info("Public key successfully fetched and cached in Gateway");
            })
            .doOnError(e -> log.error("Failed to fetch public key from auth-service", e));
    }

    private PublicKey parsePublicKey(String pem) {
        try {
            String publicKeyPEM = pem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
            byte[] keyBytes = Base64.getDecoder().decode(publicKeyPEM);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            return KeyFactory.getInstance("RSA").generatePublic(spec);
        } catch (Exception e) {
            throw new RuntimeException("Invalid public key format", e);
        }
    }

    public Mono<Claims> validateAndParseToken(String token) {
        return getPublicKey().map(key ->
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
        );
    }
}