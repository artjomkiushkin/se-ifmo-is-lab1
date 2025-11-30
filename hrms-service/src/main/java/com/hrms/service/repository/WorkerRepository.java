package com.hrms.service.repository;

import com.hrms.core.model.entity.Worker;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkerRepository extends JpaRepository<Worker, Long>, JpaSpecificationExecutor<Worker> {
    @EntityGraph(value = "Worker.full", type = EntityGraph.EntityGraphType.LOAD)
    Optional<Worker> findById(Long id);

    @EntityGraph(value = "Worker.full", type = EntityGraph.EntityGraphType.LOAD)
    Page<Worker> findAll(Pageable pageable);

    @EntityGraph(value = "Worker.full", type = EntityGraph.EntityGraphType.LOAD)
    Page<Worker> findAll(Specification<Worker> spec, Pageable pageable);
    
    Optional<Worker> findFirstByEndDate(ZonedDateTime endDate);
    
    @Query("SELECT w.id FROM Worker w WHERE w.person.id = :personId")
    List<Long> findIdsByPersonId(@Param("personId") Long personId);
    
    @Query("SELECT w.id FROM Worker w WHERE w.organization.id = :orgId")
    List<Long> findIdsByOrganizationId(@Param("orgId") Long orgId);
    
    @EntityGraph(value = "Worker.full", type = EntityGraph.EntityGraphType.LOAD)
    List<Worker> findByOrganizationId(Long organizationId);
    
    @EntityGraph(value = "Worker.full", type = EntityGraph.EntityGraphType.LOAD)
    List<Worker> findByPersonId(Long personId);
    
    long countByPerson_HeightGreaterThan(long height);
    
    @Query("SELECT DISTINCT w.startDate FROM Worker w")
    List<Date> findDistinctStartDates();
}

