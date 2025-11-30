package com.hrms.service.repository;

import com.hrms.core.model.entity.Person;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long>, JpaSpecificationExecutor<Person> {
    @EntityGraph(value = "Person.full", type = EntityGraph.EntityGraphType.LOAD)
    Optional<Person> findById(Long id);

    @EntityGraph(value = "Person.full", type = EntityGraph.EntityGraphType.LOAD)
    Page<Person> findAll(Pageable pageable);

    @EntityGraph(value = "Person.full", type = EntityGraph.EntityGraphType.LOAD)
    Page<Person> findAll(Specification<Person> spec, Pageable pageable);
    
    @Query("SELECT p.id FROM Person p WHERE p.location.id = :locationId")
    List<Long> findIdsByLocationId(@Param("locationId") Long locationId);
    
    @EntityGraph(value = "Person.full", type = EntityGraph.EntityGraphType.LOAD)
    List<Person> findByLocationId(Long locationId);
}

