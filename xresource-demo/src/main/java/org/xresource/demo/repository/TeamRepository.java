package org.xresource.demo.repository;

import org.xresource.demo.entity.Team;
import org.xresource.core.annotation.XResource;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@XResource(table = "team")
public interface TeamRepository extends JpaRepository<Team, String> {
}
