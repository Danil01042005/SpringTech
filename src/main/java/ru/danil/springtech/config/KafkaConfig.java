package ru.danil.springtech.config;

import lombok.Getter;
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
public class KafkaConfig {
    private Integer partitions;
    private Integer replicas;
    private String minInsyncReplicas;
    private String policyCreatedTopicName;

    @Bean
    NewTopic createTopic() {
        return TopicBuilder.name(policyCreatedTopicName)
                .partitions(partitions)
                .replicas(replicas)
                .configs(Map.of("min.insync.replicas", minInsyncReplicas))
                .build();
    }
}
