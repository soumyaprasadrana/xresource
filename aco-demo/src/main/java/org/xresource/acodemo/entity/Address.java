package org.xresource.acodemo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long addressId;

    private String street;
    private String postalCode;
    private String type;

    @ManyToOne
    @JoinColumn(name = "city_id")
    private City city;

    @OneToMany(mappedBy = "address")
    private List<Organization> organizations;

    @OneToMany(mappedBy = "address")
    private List<Person> persons;

    @OneToMany(mappedBy = "address")
    private List<AssetLocation> assetLocations;

}