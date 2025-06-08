package org.xresource.core.aco.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "aco_path_request")
@Data
public class AcoPathRequest implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "request_time", nullable = false)
    private LocalDateTime requestTime = LocalDateTime.now();

    @Column(name = "path", nullable = false, columnDefinition = "TEXT")
    private String path;

    @Column(name = "cost", nullable = false)
    private Double cost;

    @Column(name = "pheromone_contribution", nullable = false)
    private Double pheromoneContribution;

    @Column(name = "materialized_view_created", nullable = false)
    private Boolean materializedViewCreated = false;

}
