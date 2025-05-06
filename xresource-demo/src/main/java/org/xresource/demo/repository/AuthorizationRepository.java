package org.xresource.demo.repository;

import org.xresource.demo.entity.Authorization;
import org.xresource.core.annotation.XResource;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@XResource(table = "authorizations")
public interface AuthorizationRepository extends JpaRepository<Authorization, String> {
    Optional<Authorization> findByUserId(String userId);
}
