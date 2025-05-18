package org.xresource.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.xresource.core.annotations.XMetadata;

@Entity
@Table(name = "session", schema = "xresourcedemo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@XMetadata(path = "schemas/session.json")
public class Session {

    @Id
    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Column(name = "auth_token", nullable = false)
    private String authToken;

    @Column(name = "start_timestamp", nullable = false)
    private String startTimestamp;

    @Column(name = "last_activity_timestamp", nullable = false)
    private String lastActivityTimestamp;

    @Column(name = "client_ip", nullable = false)
    private String clientIp;

    @Column(name = "client_os", nullable = false)
    private String clientOs;

    @Column(name = "client_browser", nullable = false)
    private String clientBrowser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "user_id", foreignKey = @ForeignKey(name = "fk_session_user"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_team", nullable = false, referencedColumnName = "team_id", foreignKey = @ForeignKey(name = "fk_session_team"))
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Team userTeam;
}
