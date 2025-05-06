package org.xresource.demo.entity;

import org.xresource.core.annotation.XMetadata;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "authorizations", schema = "xresourcedemo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@XMetadata(path = "schemas/authorization.json")
public class Authorization {

    @Id
    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "user_team", nullable = false)
    private String userTeam;

    @Column(name = "is_admin", nullable = false)
    private boolean isAdmin;

    public boolean isAdmin() {
        return this.isAdmin;
    }
}
