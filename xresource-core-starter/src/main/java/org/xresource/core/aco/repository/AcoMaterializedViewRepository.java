package org.xresource.core.aco.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.xresource.core.aco.entity.AcoMaterializedView;

@Repository
public interface AcoMaterializedViewRepository extends JpaRepository<AcoMaterializedView, Integer> {

}
