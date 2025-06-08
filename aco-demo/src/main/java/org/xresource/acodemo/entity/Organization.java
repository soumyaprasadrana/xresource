package org.xresource.acodemo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class Organization {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orgId;

    private String name;
    private String type;

    @ManyToOne
    @JoinColumn(name = "address_id")
    private Address address;

    @OneToMany(mappedBy = "organization")
    private List<Person> persons;

    @OneToMany(mappedBy = "organization")
    private List<Project> projects;

    @OneToMany(mappedBy = "ownerOrganization")
    private List<Asset> assets;

    // getters and setters
}