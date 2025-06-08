package org.xresource.acodemo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
public class AssetMaintenance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long assetMaintenanceId;

    private Date date;
    private String details;

    @ManyToOne
    @JoinColumn(name = "asset_id")
    private Asset asset;

    @ManyToOne
    @JoinColumn(name = "performed_by_pm_id")
    private ProjectMember performedByProjectMember;

    // getters and setters
}