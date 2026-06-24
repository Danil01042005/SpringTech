package ru.danil.springtech.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;
import ru.danil.springtech.model.enums.ActionName;
import ru.danil.springtech.model.enums.DeferredJobStatus;
import ru.danil.springtech.model.enums.EntityName;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@SQLDelete(sql = "UPDATE test.jobs SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Table(name = "jobs", schema = "test")
public class DeferredJob {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID jobId;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_name", nullable = false)
    private EntityName entityName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "jsonb")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_name", nullable = false)
    private ActionName actionName;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_status")
    private DeferredJobStatus deferredJobStatus;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @PrePersist
    private void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    private void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
