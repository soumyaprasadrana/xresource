package org.xresource.acodemo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
public class AssetUsage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long assetUsageId;

    private Date fromDate;
    private Date toDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id")
    private Asset asset;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "used_by_pm_id")
    private ProjectMember usedByProjectMember;

    // getters and setters
}