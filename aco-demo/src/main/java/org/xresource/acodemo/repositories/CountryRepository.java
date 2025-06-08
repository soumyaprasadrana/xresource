package org.xresource.acodemo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.xresource.acodemo.entity.Country;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {
}