package org.xresource.demo.repository;

import org.xresource.core.annotations.XCronResource;
import org.xresource.core.annotations.XQuery;
import org.xresource.core.annotations.XResourceIgnore;
import org.xresource.demo.entity.Session;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@XResourceIgnore
@XCronResource
@XQuery(name = "expiredSessions", where = "1=1")
public interface SessionRepository extends JpaRepository<Session, String> {

    Optional<Session> findByAuthToken(String authToken);

    List<Session> findByUser_UserId(String userId);

    List<Session> findByUserTeam_TeamId(String teamId);
}
