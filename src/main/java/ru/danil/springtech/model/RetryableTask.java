package ru.danil.springtech.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import ru.danil.springtech.kafka.dto.RetryableTaskStatus;
import ru.danil.springtech.kafka.dto.RetryableTaskType;
import ru.danil.springtech.util.converter.RetryableTaskStatusConverter;
import ru.danil.springtech.util.converter.RetryableTaskTypeConverter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "retryable_task")
@SQLDelete(sql = "UPDATE test.retryable_task SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@ToString
public class RetryableTask {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "payload", columnDefinition = "jsonb")
    @ColumnTransformer(write = "?::jsonb")
    private String payload;

    @Column(name = "type")
    @Convert(converter = RetryableTaskTypeConverter.class)
    private RetryableTaskType type;

    @Column(name = "status")
    @Convert(converter = RetryableTaskStatusConverter.class)
    private RetryableTaskStatus status;

    @Column(name = "retry_time")
    private Instant retryTime;

    @Column(name = "attempts")
    private Integer attempts;

    @Column(nullable = true)
    private Instant leaseExpiresAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;
}
