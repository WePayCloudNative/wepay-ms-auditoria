
package cl.duoc.wepay_ms_auditoria.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI auditoriaOpenAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("WePay - API de Auditoría")
                        .version("1.0.0")
                        .description(
                            "Microservicio de auditoría de WePay. "
                            + "Consume eventos desde Apache Kafka, "
                            + "almacena el historial de operaciones "
                            + "y permite consultar eventos por pedido "
                            + "o usuario."
                        ));
    }
}
