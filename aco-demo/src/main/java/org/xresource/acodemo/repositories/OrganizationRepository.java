package org.xresource.acodemo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.xresource.acodemo.entity.Organization;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
}