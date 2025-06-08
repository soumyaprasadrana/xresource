package org.xresource.acodemo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.xresource.acodemo.entity.Person;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
}