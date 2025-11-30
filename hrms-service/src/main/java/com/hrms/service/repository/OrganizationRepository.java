package com.hrms.service.repository;

import com.hrms.core.model.entity.Organization;
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
public interface OrganizationRepository extends JpaRepository<Organization, Long>, JpaSpecificationExecutor<Organization> {
    @EntityGraph(value = "Organization.full", type = EntityGraph.EntityGraphType.LOAD)
    Optional<Organization> findById(Long id);

    @EntityGraph(value = "Organization.full", type = EntityGraph.EntityGraphType.LOAD)
    Page<Organization> findAll(Pageable pageable);

    @EntityGraph(value = "Organization.full", type = EntityGraph.EntityGraphType.LOAD)
    Page<Organization> findAll(Specification<Organization> spec, Pageable pageable);
    
    @Query("SELECT o.id FROM Organization o WHERE o.officialAddress.id = :addressId")
    List<Long> findIdsByOfficialAddressId(@Param("addressId") Long addressId);
    
    @EntityGraph(value = "Organization.full", type = EntityGraph.EntityGraphType.LOAD)
    List<Organization> findByOfficialAddressId(Long addressId);
}

