package org.xresource.acodemo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.xresource.acodemo.entity.Project;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
}