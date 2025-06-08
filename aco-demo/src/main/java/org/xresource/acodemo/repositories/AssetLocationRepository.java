package org.xresource.acodemo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.xresource.acodemo.entity.AssetLocation;

@Repository
public interface AssetLocationRepository extends JpaRepository<AssetLocation, Long> {
}