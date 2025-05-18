package org.xresource.demo.repository;

import org.xresource.core.annotations.XResourceExposeAsEmbeddedResource;
import org.xresource.core.annotations.XResourceIgnore;
import org.xresource.demo.entity.Team;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
// @XResourceIgnore
// @XResourceExposeAsEmbeddedResource
public interface TeamRepository extends JpaRepository<Team, String> {
}
