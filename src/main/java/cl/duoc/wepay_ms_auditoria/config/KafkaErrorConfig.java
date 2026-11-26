package cl.duoc.wepay_ms_auditoria.config;

import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Si un evento falla (JSON inválido, fecha mal formada, base caída), se reintenta 2 veces
 * con 1 segundo de espera y luego se envía a un Dead Letter Topic propio de auditoría,
 * en vez de perderse. Spring Boot aplica este manejador al listener automáticamente.
 */
@Configuration
public class KafkaErrorConfig {

    @Bean
    public CommonErrorHandler kafkaErrorHandler(
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${app.kafka.topic.dlt}") String topicoDlt) {

        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (registro, error) -> new TopicPartition(topicoDlt, -1));

        return new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 2L));
    }
}
