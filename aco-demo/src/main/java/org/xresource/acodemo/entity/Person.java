package org.xresource.acodemo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long personId;

    private String name;
    private String role;

    @ManyToOne
    @JoinColumn(name = "org_id")
    private Organization organization;

    @ManyToOne
    @JoinColumn(name = "address_id")
    private Address address;

    @ManyToOne
    @JoinColumn(name = "supervisor_id")
    private Person supervisor;

    @OneToMany(mappedBy = "person")
    private List<ProjectMember> projectMemberships;

    @OneToMany(mappedBy = "supervisor")
    private List<Person> subordinates;

    // getters and setters
}