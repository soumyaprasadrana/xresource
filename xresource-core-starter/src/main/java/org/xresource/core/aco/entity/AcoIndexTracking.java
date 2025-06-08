package org.xresource.core.aco.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "aco_index_tracking")
@Data
public class AcoIndexTracking implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "index_name", nullable = false, unique = true)
    private String indexName;

    @Column(name = "table_name", nullable = false)
    private String tableName;

    @Column(name = "columns", nullable = false, columnDefinition = "TEXT")
    private String columns;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(name = "usage_count", nullable = false)
    private Long usageCount = 0L;

}
