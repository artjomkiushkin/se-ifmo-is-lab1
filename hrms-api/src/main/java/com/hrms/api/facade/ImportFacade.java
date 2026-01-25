package com.hrms.api.facade;

import com.hrms.api.mapper.ImportOperationFacadeMapper;
import com.hrms.api.request.imports.*;
import com.hrms.api.response.ImportOperationResponse;
import com.hrms.service.service.ImportService;
import com.hrms.service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImportFacade {
    private final ImportService importService;
    private final ImportOperationFacadeMapper mapper;
    private final NotificationService notificationService;
    private final WorkerFacade workerFacade;
    private final OrganizationFacade organizationFacade;
    private final PersonFacade personFacade;
    private final LocationFacade locationFacade;
    private final AddressFacade addressFacade;

    public ImportOperationResponse importEntities(List<ImportItem> items, Long userId, String username) {
        var operation = importService.createOperation(userId, username);
        Set<Class<?>> importedTypes = new HashSet<>();
        try {
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
            var result = mapper.toResponse(importService.markSuccess(operation.getId(), count));
            notifyRefreshAfterImport(importedTypes);
            return result;
        } catch (Exception e) {
            log.error("Import failed", e);
            NotificationService.setImportMode(false);
            return mapper.toResponse(importService.markFailed(operation.getId(), e.getMessage()));
        }
    }

    private void notifyRefreshAfterImport(Set<Class<?>> importedTypes) {
        var needsWorkerRefresh = false;
        var needsOrganizationRefresh = false;
        var needsPersonRefresh = false;
        
        if (importedTypes.contains(WorkerImportItem.class)) {
            needsWorkerRefresh = true;
        }
        if (importedTypes.contains(OrganizationImportItem.class)) {
            needsOrganizationRefresh = true;
            needsWorkerRefresh = true;
        }
        if (importedTypes.contains(PersonImportItem.class)) {
            needsPersonRefresh = true;
            needsWorkerRefresh = true;
        }
        if (importedTypes.contains(LocationImportItem.class)) {
            needsPersonRefresh = true;
            needsWorkerRefresh = true;
        }
        if (importedTypes.contains(AddressImportItem.class)) {
            needsOrganizationRefresh = true;
            needsWorkerRefresh = true;
        }
        
        if (needsOrganizationRefresh) {
            notificationService.notifyOrganizationsRefresh();
        }
        if (needsPersonRefresh) {
            notificationService.notifyPersonsRefresh();
        }
        if (needsWorkerRefresh) {
            notificationService.notifyWorkersRefresh();
        }
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

    public Page<ImportOperationResponse> getHistory(Long userId, boolean isAdmin, Pageable pageable) {
        log.info("getHistory: userId={}, isAdmin={}", userId, isAdmin);
        if (isAdmin) {
            log.info("Fetching all import operations (admin mode)");
            return importService.findAll(pageable).map(mapper::toResponse);
        }
        log.info("Fetching import operations for userId={}", userId);
        return importService.findByUserId(userId, pageable).map(mapper::toResponse);
    }

    public ImportOperationResponse getById(Long id) {
        return mapper.toResponse(importService.findById(id));
    }
}

