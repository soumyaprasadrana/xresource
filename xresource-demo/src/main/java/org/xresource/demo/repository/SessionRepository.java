package org.xresource.demo.repository;

import org.xresource.demo.entity.Session;
import org.xresource.core.annotation.XResource;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@XResource(table = "session")
public interface SessionRepository extends JpaRepository<Session, String> {

    Optional<Session> findByAuthToken(String authToken);

    List<Session> findByUser_UserId(String userId);

    List<Session> findByUserTeam_TeamId(String teamId);
}
