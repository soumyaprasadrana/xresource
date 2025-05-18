package org.xresource.demo.entity;

import org.xresource.core.annotations.XMetadata;

import io.swagger.v3.oas.annotations.Hidden;
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

    @MapsId
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", foreignKey = @ForeignKey(name = "fk_authorization_user"))
    @Hidden
    private User user;

    public boolean isAdmin() {
        return this.isAdmin;
    }
}
