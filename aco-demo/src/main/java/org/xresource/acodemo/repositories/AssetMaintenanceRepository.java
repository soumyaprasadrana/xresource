package org.xresource.acodemo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.xresource.acodemo.entity.AssetMaintenance;

@Repository
public interface AssetMaintenanceRepository extends JpaRepository<AssetMaintenance, Long> {
}