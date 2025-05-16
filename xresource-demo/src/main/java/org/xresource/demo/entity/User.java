package org.xresource.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import org.xresource.core.annotations.AccessLevel;
import org.xresource.core.annotations.XControlledByAction;
import org.xresource.core.annotations.XFieldAction;
import org.xresource.core.annotations.XHidden;
import org.xresource.core.annotations.XJSONFormFieldMetadata;
import org.xresource.core.annotations.XMetadata;
import org.xresource.core.annotations.XResourceAuthGroup;
import org.xresource.core.annotations.XResourceAuthGroups;

@Entity
@Table(name = "user", schema = "xresourcedemo", indexes = {
        @Index(name = "idx_user_email", columnList = "email")
})
@XMetadata(path = "schemas/user.json")
@Getter
@Setter
public class User {

    @Id
    @Column(name = "user_id", nullable = false, length = 255)
    @XJSONFormFieldMetadata(description = "User ID", displaySeq = 1, label = "User ID")
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_team", nullable = false, referencedColumnName = "team_id", foreignKey = @ForeignKey(name = "fk_user_team"))
    private Team team;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @XJSONFormFieldMetadata(description = "Authorization of the user", displaySeq = 3, label = "Authorization")
    private Authorization authorization;

    @Column(name = "user_pass", nullable = false, columnDefinition = "TEXT")
    @XHidden
    @XJSONFormFieldMetadata(description = "Password of the user", displaySeq = 2, label = "Password")
    private String userPass;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    @Email
    private String email;

    @Column(name = "first_name", length = 100)
    @XJSONFormFieldMetadata(description = "First Name of the user", displaySeq = 2, label = "First Name")
    private String firstName;

    @Column(name = "last_name", length = 100)
    // @XResourceAuthGroups(value = { @XResourceAuthGroup(role = "*", access =
    // AccessLevel.NONE) })
    @XJSONFormFieldMetadata(description = "Last Name of the user", displaySeq = 3, label = "Last Name")
    @XControlledByAction(actions = {
            @XFieldAction(name = "updateLastNameToSoumya", value = "soumya"),
            @XFieldAction(name = "updateLastNameToNilesh", value = "Nilesh")
    }, allowInsert = false, allowUpdate = false)
    private String lastName;

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
