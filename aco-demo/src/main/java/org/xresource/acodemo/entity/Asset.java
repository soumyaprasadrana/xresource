package org.xresource.acodemo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long assetId;

    private String tag;
    private String description;
    private String type;

    @ManyToOne
    @JoinColumn(name = "owner_org_id")
    private Organization ownerOrganization;

    @OneToMany(mappedBy = "asset")
    private List<AssetLocation> assetLocations;

    @OneToMany(mappedBy = "asset")
    private List<AssetUsage> assetUsages;

    @OneToMany(mappedBy = "asset")
    private List<AssetMaintenance> assetMaintenances;

    // getters and setters
}