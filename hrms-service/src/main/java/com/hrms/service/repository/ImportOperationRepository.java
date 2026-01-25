package com.hrms.service.repository;

import com.hrms.core.model.entity.ImportOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImportOperationRepository extends JpaRepository<ImportOperation, Long> {
    Page<ImportOperation> findByUserId(Long userId, Pageable pageable);
    Page<ImportOperation> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<ImportOperation> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}

