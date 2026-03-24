package me.xpestilent.auth.impl.service.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import me.xpestilent.auth.api.dto.response.GeneratedToken;
import me.xpestilent.auth.impl.entity.UserEntity;
import me.xpestilent.auth.impl.service.JwtService;
import me.xpestilent.foundation.web.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class JwtServiceImpl implements JwtService {

    private final Resource publicKeyResource;
    private String publicKeyPemString;
    private PublicKey publicKey;
    private final PrivateKey privateKey;
    private final String issuer;
    private final long accessExpiration;
    private final long refreshExpiration;
    private final String accessTokenType;
    private final String refreshTokenType;

    public JwtServiceImpl(
        @Value("${foundation.security.jwt.private-key-path}") Resource privateKeyResource,
        @Value("${foundation.security.jwt.public-key-path}") Resource publicKeyResource,
        @Value("${spring.application.name}") String issuer,
        @Value("${foundation.security.jwt.expiration.access}") long accessExpiration,
        @Value("${foundation.security.jwt.expiration.refresh}") long refreshExpiration,
        @Value("${foundation.security.jwt.type.access}") String accessTokenType,
        @Value("${foundation.security.jwt.type.refresh}") String refreshTokenType,
        @Value("${foundation.security.jwt.algorithm}") String algorithm

    ) throws Exception {

        this.issuer = issuer;
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
        this.accessTokenType = accessTokenType;
        this.refreshTokenType = refreshTokenType;
        this.publicKeyResource = publicKeyResource;


        this.publicKeyPemString = new String(publicKeyResource.getInputStream().readAllBytes());

        String publicKeyContent = publicKeyPemString
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replaceAll("\\s+", "");

        byte[] pubKeyBytes = Base64.getDecoder().decode(publicKeyContent);
        X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(pubKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        this.publicKey = keyFactory.generatePublic(pubSpec);

        log.info("Публичный ключ успешно загружен!");

        try (InputStream is = privateKeyResource.getInputStream()) {
            byte[] keyBytes = is.readAllBytes();
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance(algorithm);
            this.privateKey = kf.generatePrivate(spec);
            log.info("Private key successfully loaded");
        } catch (Exception e) {
            log.error("Failed to load private key", e);
            throw e;
        }
    }

    public GeneratedToken generateAccessToken(UserEntity user, UUID sessionId) {
        var claims = Jwts.claims()
            .subject(user.getId().toString())
            .add("typ", accessTokenType)
            .add("sid", sessionId.toString())
            .add("username", user.getUsername())
            .add("roles", user.getRoles().stream()
                .map(ur -> ur.getRole().getName())
                .toList())
            .build();

        return buildToken(claims, accessExpiration);
    }

    public GeneratedToken generateRefreshToken(UserEntity user, UUID jti) {
        var claims = Jwts.claims()
            .subject(user.getId().toString())
            .add("jti", jti.toString())
            .add("typ", refreshTokenType)
            .build();

        return buildToken(claims, refreshExpiration);
    }

    private GeneratedToken buildToken(Map<String, Object> claims, long expirationMs) {
        Instant expiresAt = Instant.now().plusMillis(expirationMs);

        String token = Jwts.builder()
            .claims(claims)
            .issuer(issuer)
            .issuedAt(new Date())
            .expiration(Date.from(expiresAt))
            .signWith(privateKey, Jwts.SIG.RS256)
            .compact();
        return new GeneratedToken(token, expiresAt);
    }

    @Override
    public String getPublicKeyPem() {
        try {
            return new String(publicKeyResource.getInputStream().readAllBytes());
        } catch (Exception e) {
            throw new RuntimeException("Не удалось прочитать публичный ключ", e);
        }
    }

    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        } catch (JwtException e) {
            throw new BusinessException("Невалидный или просроченный токен обновления", "INVALID_TOKEN", HttpStatus.UNAUTHORIZED);
        }
    }
}
