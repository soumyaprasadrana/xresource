package org.xresource.acodemo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
public class ProjectMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pmId;

    private String role;
    private Date joinedOn;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne
    @JoinColumn(name = "person_id")
    private Person person;

    @OneToMany(mappedBy = "usedByProjectMember")
    private List<AssetUsage> assetUsages;

    @OneToMany(mappedBy = "performedByProjectMember")
    private List<AssetMaintenance> assetMaintenances;

    // getters and setters
}