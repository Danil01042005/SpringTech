package ru.danil.springtech.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "kafka-topic")
@Getter
@Setter
@RequiredArgsConstructor
public class KafkaConfig {
    private final KafkaConfigProperties properties;

    @Bean
    NewTopic createTopic() {
        return TopicBuilder.name(properties.getPolicyCreatedTopicName())
                .partitions(properties.getPartitions())
                .replicas(properties.getReplicas())
                .configs(Map.of("min.insync.replicas", properties.getMinInsyncReplicas()))
                .build();
    }
}
