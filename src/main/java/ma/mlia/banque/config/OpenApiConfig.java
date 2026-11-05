package ma.mlia.banque.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI metadata rendered by Swagger UI.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bankOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Bank Accounts API")
                .description("REST API built with Spring MVC @RestController, serving JSON and XML")
                .version("1.0.0"));
    }
}
