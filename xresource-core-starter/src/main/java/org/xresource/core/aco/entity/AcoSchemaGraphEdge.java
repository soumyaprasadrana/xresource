package org.xresource.core.aco.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "aco_schema_graph_edge", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "from_table", "to_table" })
})
@Data
public class AcoSchemaGraphEdge implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "from_table", nullable = false)
    private String fromTable;

    @Column(name = "to_table", nullable = false)
    private String toTable;

    @Column(name = "join_condition", nullable = false, columnDefinition = "TEXT")
    private String joinCondition;

    @Column(name = "latency", nullable = false)
    private Double latency = 1.0;

    @Column(name = "pheromone_level", nullable = false)
    private Double pheromoneLevel = 0.1;

    @Column(name = "last_decay", nullable = false)
    private LocalDateTime lastDecay = LocalDateTime.now();

    @Column(name = "join_hash", nullable = false)
    private String joinHash;

    @Column(name = "hash_updated", nullable = false)
    private Boolean hashUpdated = true;

}
