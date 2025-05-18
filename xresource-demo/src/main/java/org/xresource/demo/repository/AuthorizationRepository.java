package org.xresource.demo.repository;

import org.xresource.core.annotations.XResourceExposeAsEmbeddedResource;
import org.xresource.core.annotations.XResourceIgnore;
import org.xresource.demo.entity.Authorization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@XResourceIgnore
@XResourceExposeAsEmbeddedResource
public interface AuthorizationRepository extends JpaRepository<Authorization, String> {
    Optional<Authorization> findByUserId(String userId);
}
