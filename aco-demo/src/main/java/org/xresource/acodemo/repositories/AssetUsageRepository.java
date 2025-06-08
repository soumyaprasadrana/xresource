package org.xresource.acodemo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.xresource.acodemo.entity.AssetUsage;

@Repository
public interface AssetUsageRepository extends JpaRepository<AssetUsage, Long> {
}