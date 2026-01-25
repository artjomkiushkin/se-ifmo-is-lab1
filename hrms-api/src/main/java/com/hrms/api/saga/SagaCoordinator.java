package com.hrms.service.saga;

import com.hrms.api.facade.*;
import com.hrms.api.request.imports.*;
import com.hrms.core.mapper.SagaTransactionMapper;
import com.hrms.core.model.dto.SagaTransactionDTO;
import com.hrms.core.model.entity.SagaTransaction;
import com.hrms.core.model.enums.SagaStatus;
import com.hrms.core.model.enums.SagaType;
import com.hrms.service.exception.BusinessException;
import com.hrms.service.repository.SagaTransactionRepository;
import com.hrms.service.service.ImportService;
import com.hrms.service.service.MinioService;
import com.hrms.service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class SagaCoordinator {
    private final SagaTransactionRepository sagaRepository;
    private final SagaTransactionMapper sagaMapper;
    private final MinioService minioService;
    private final ImportService importService;
    private final NotificationService notificationService;
    private final WorkerFacade workerFacade;
    private final OrganizationFacade organizationFacade;
    private final PersonFacade personFacade;
    private final LocationFacade locationFacade;
    private final AddressFacade addressFacade;

    public SagaTransactionDTO executeImportSaga(
        MultipartFile file,
        List<ImportItem> items,
        Long userId,
        String username
    ) {
        var saga = createSaga(SagaType.IMPORT);
        
        try {
            var objectName = minioService.uploadFile(
                file.getOriginalFilename(),
                file.getInputStream(),
                file.getSize(),
                file.getContentType()
            );
            saga = updateSagaStatus(
                saga.getId(), 
                SagaStatus.FILE_UPLOADED, 
                objectName, 
                file.getOriginalFilename(),
                file.getSize()
            );
            
            var operation = importService.createOperation(userId, username);
            saga.setImportOperationId(operation.getId());
            saga = sagaRepository.save(saga);
            
            Set<Class<?>> importedTypes = new HashSet<>();
            NotificationService.setImportMode(true);
            
            var count = importService.executeInSerializableTransaction(() -> {
                int imported = 0;
                for (var item : items) {
                    processItem(item);
                    importedTypes.add(item.getClass());
                    imported++;
                }
                return imported;
            });
            
            NotificationService.setImportMode(false);
            
            var finalOperation = importService.markSuccess(operation.getId(), count);
            updateImportOperationFile(finalOperation.getId(), file.getOriginalFilename(), file.getSize());
            
            saga = updateSagaStatus(saga.getId(), SagaStatus.COMPLETED, null, null, null);
            
            notifyRefreshAfterImport(importedTypes);
            notificationService.notifyImportOperationUpdated(finalOperation);
            
            return sagaMapper.toDTO(saga);
        } catch (Exception e) {
            log.error("Saga failed, rolling back", e);
            NotificationService.setImportMode(false);
            rollbackSaga(saga, e.getMessage());
            throw new BusinessException("Импорт не выполнен: " + e.getMessage());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected SagaTransaction createSaga(SagaType type) {
        var saga = SagaTransaction.builder()
            .sagaType(type)
            .status(SagaStatus.STARTED)
            .createdAt(ZonedDateTime.now())
            .updatedAt(ZonedDateTime.now())
            .build();
        return sagaRepository.save(saga);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected SagaTransaction updateSagaStatus(
        Long sagaId, 
        SagaStatus status, 
        String minioObjectName,
        String originalFilename,
        Long fileSize
    ) {
        var saga = sagaRepository.findById(sagaId)
            .orElseThrow(() -> new BusinessException("Saga транзакция не найдена"));
        saga.setStatus(status);
        if (minioObjectName != null) {
            saga.setMinioObjectName(minioObjectName);
        }
        if (originalFilename != null) {
            saga.setOriginalFilename(originalFilename);
        }
        if (fileSize != null) {
            saga.setFileSize(fileSize);
        }
        saga.setUpdatedAt(ZonedDateTime.now());
        return sagaRepository.save(saga);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void updateImportOperationFile(Long operationId, String fileName, Long fileSize) {
        importService.updateFileInfo(operationId, fileName, fileSize);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void rollbackSaga(SagaTransaction saga, String errorMessage) {
        if (saga.getMinioObjectName() != null) {
            try {
                minioService.deleteFile(saga.getMinioObjectName());
                log.info("File deleted from MinIO during rollback: {}", saga.getMinioObjectName());
            } catch (Exception e) {
                log.error("Failed to delete file from MinIO during rollback", e);
            }
        }
        
        if (saga.getImportOperationId() != null) {
            try {
                var operation = importService.markFailed(saga.getImportOperationId(), errorMessage);
                notificationService.notifyImportOperationUpdated(operation);
            } catch (Exception e) {
                log.error("Failed to mark import operation as failed", e);
            }
        }
        
        saga.setStatus(SagaStatus.ROLLED_BACK);
        saga.setErrorMessage(errorMessage);
        saga.setUpdatedAt(ZonedDateTime.now());
        sagaRepository.save(saga);
    }

    private void processItem(ImportItem item) {
        if (item instanceof WorkerImportItem w) {
            workerFacade.createWorker(w);
        } else if (item instanceof OrganizationImportItem o) {
            organizationFacade.createOrganization(o);
        } else if (item instanceof PersonImportItem p) {
            personFacade.createPerson(p);
        } else if (item instanceof LocationImportItem l) {
            locationFacade.createLocation(l);
        } else if (item instanceof AddressImportItem a) {
            addressFacade.createAddress(a);
        } else {
            throw new IllegalArgumentException("Неизвестный тип объекта для импорта");
        }
    }

    private void notifyRefreshAfterImport(Set<Class<?>> importedTypes) {
        if (!importedTypes.isEmpty()) {
            notificationService.notifyWorkersRefresh();
            notificationService.notifyOrganizationsRefresh();
            notificationService.notifyPersonsRefresh();
        }
    }

    public SagaTransactionDTO findByImportOperationId(Long importOperationId) {
        return sagaRepository.findByImportOperationId(importOperationId)
            .map(sagaMapper::toDTO)
            .orElseThrow(() -> new BusinessException("Saga транзакция не найдена для операции импорта: " + importOperationId));
    }
}

