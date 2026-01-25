package com.hrms.service.service;

import com.hrms.core.mapper.ImportOperationMapper;
import com.hrms.core.model.dto.ImportOperationDTO;
import com.hrms.core.model.entity.ImportOperation;
import com.hrms.core.model.enums.ImportStatus;
import com.hrms.service.exception.BusinessException;
import com.hrms.service.exception.EntityNotFoundException;
import com.hrms.service.repository.ImportOperationRepository;
import jakarta.persistence.PersistenceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportService {
    private final ImportOperationRepository importOperationRepository;
    private final ImportOperationMapper importOperationMapper;
    private final NotificationService notificationService;

    public ImportOperationDTO createOperation(Long userId, String username) {
        var operation = ImportOperation.builder()
            .status(ImportStatus.IN_PROGRESS)
            .userId(userId)
            .username(username)
            .createdAt(ZonedDateTime.now())
            .build();
        var dto = importOperationMapper.toDTO(importOperationRepository.save(operation));
        notificationService.notifyImportOperationUpdated(dto);
        return dto;
    }

    public ImportOperationDTO markSuccess(Long operationId, int addedCount) {
        var operation = importOperationRepository.findById(operationId)
            .orElseThrow(() -> new EntityNotFoundException("Операция импорта не найдена"));
        operation.setStatus(ImportStatus.SUCCESS);
        operation.setAddedCount(addedCount);
        operation.setFinishedAt(ZonedDateTime.now());
        var dto = importOperationMapper.toDTO(importOperationRepository.save(operation));
        notificationService.notifyImportOperationUpdated(dto);
        return dto;
    }

    public ImportOperationDTO markFailed(Long operationId, String errorMessage) {
        var operation = importOperationRepository.findById(operationId)
            .orElseThrow(() -> new EntityNotFoundException("Операция импорта не найдена"));
        operation.setStatus(ImportStatus.FAILED);
        operation.setErrorMessage(errorMessage);
        operation.setFinishedAt(ZonedDateTime.now());
        var dto = importOperationMapper.toDTO(importOperationRepository.save(operation));
        notificationService.notifyImportOperationUpdated(dto);
        return dto;
    }

    public Page<ImportOperationDTO> findAll(Pageable pageable) {
        return importOperationRepository.findAllByOrderByCreatedAtDesc(pageable)
            .map(importOperationMapper::toDTO);
    }

    public Page<ImportOperationDTO> findByUserId(Long userId, Pageable pageable) {
        return importOperationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
            .map(importOperationMapper::toDTO);
    }

    public ImportOperationDTO findById(Long id) {
        return importOperationRepository.findById(id)
            .map(importOperationMapper::toDTO)
            .orElseThrow(() -> new EntityNotFoundException("Операция импорта не найдена"));
    }

    @Retryable(
        retryFor = {CannotAcquireLockException.class, PersistenceException.class, DataAccessException.class},
        noRetryFor = {BusinessException.class, IllegalArgumentException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 100, multiplier = 2)
    )
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public <T> T executeInSerializableTransaction(Supplier<T> action) {
        return action.get();
    }
}

