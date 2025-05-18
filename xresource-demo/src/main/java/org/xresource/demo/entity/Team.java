package org.xresource.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

import org.xresource.core.annotations.XMetadata;
import org.xresource.core.annotations.XResourceAccess;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({ "users", "hibernateLazyInitializer", "handler" })
@Entity
@Table(name = "team", schema = "xresourcedemo")
@XMetadata(path = "schemas/team.json")
@XResourceAccess(readRoles = { "ROLE_USER" })
public class Team {

    @Id
    @Column(name = "team_id", nullable = false, length = 100)
    private String teamId;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Relationships (reverse mappings)
    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<User> users;

    // Getters and setters
    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}
