package me.xpestilent.foundation.web.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;


@AutoConfiguration
public class SwaggerAutoConfiguration {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
            .addServersItem(new Server().url("/"))
            .info(new Info()
                .title("Auth Service API")
                .version("1.0")
                .description("API для аутентификации и управления токенами"))
            .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
            .components(new Components()
                .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                    .name(securitySchemeName)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("Введите токен (слово Bearer подставится автоматически)")));
    }

    @Bean
    public GlobalOpenApiCustomizer globalHeaderCustomizer() {
        return openApi -> {
            if (openApi.getPaths() != null) {
                openApi.getPaths().values().forEach(pathItem -> pathItem.readOperations().forEach(operation -> {
                    operation.addParametersItem(new Parameter()
                        .in("header")
                        .name("X-Device-Id")
                        .description("Идентификатор устройства (опционально)")
                        .required(false)
                        .schema(new io.swagger.v3.oas.models.media.StringSchema()));
                }));
            }
        };
    }
}