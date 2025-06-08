package org.xresource.acodemo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
public class AssetLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long assetLocationId;

    @ManyToOne
    @JoinColumn(name = "asset_id")
    private Asset asset;

    @ManyToOne
    @JoinColumn(name = "address_id")
    private Address address;

    private Date fromDate;
    private Date toDate;

    // getters and setters
}