package com.hrms.service.service;

import com.hrms.core.model.dto.AddressDTO;
import com.hrms.core.model.dto.ImportOperationDTO;
import com.hrms.core.model.dto.LocationDTO;
import com.hrms.core.model.dto.OrganizationDTO;
import com.hrms.core.model.dto.PersonDTO;
import com.hrms.core.model.dto.WorkerDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    
    private final SimpMessagingTemplate messagingTemplate;
    private static final ThreadLocal<Boolean> IMPORT_MODE = ThreadLocal.withInitial(() -> false);
    
    public static void setImportMode(boolean enabled) {
        IMPORT_MODE.set(enabled);
    }
    
    private boolean isImportMode() {
        return IMPORT_MODE.get();
    }
    
    public void notifyImportOperationUpdated(ImportOperationDTO dto) {
        messagingTemplate.convertAndSend("/topic/imports/updated", dto);
    }
    
    // private static final String TOPIC_PREFIX = "/topic/";
    
    public void notifyWorkerCreated(WorkerDTO workerDTO) {
        if (isImportMode()) return;
        messagingTemplate.convertAndSend("/topic/workers/created", workerDTO);
    }
    
    public void notifyWorkerUpdated(WorkerDTO workerDTO) {
        if (isImportMode()) return;
        messagingTemplate.convertAndSend("/topic/workers/updated", workerDTO);
    }
    
    public void notifyWorkerDeleted(Long workerId) {
        if (isImportMode()) return;
        messagingTemplate.convertAndSend("/topic/workers/deleted", workerId);
    }
    
    public void notifyWorkersRefresh() {
        if (isImportMode()) return;
        messagingTemplate.convertAndSend("/topic/workers/refresh", "refresh");
    }
    
    public void notifyOrganizationCreated(OrganizationDTO dto) {
        if (isImportMode()) return;
        messagingTemplate.convertAndSend("/topic/organizations/created", dto);
    }
    
    public void notifyOrganizationUpdated(OrganizationDTO dto) {
        if (isImportMode()) return;
        messagingTemplate.convertAndSend("/topic/organizations/updated", dto);
        notifyWorkersRefresh();
    }
    
    public void notifyOrganizationDeleted(Long id) {
        if (isImportMode()) return;
        messagingTemplate.convertAndSend("/topic/organizations/deleted", id);
        notifyWorkersRefresh();
    }
    
    public void notifyLocationCreated(LocationDTO locationDTO) {
        if (isImportMode()) return;
        messagingTemplate.convertAndSend("/topic/locations/created", locationDTO);
    }
    
    public void notifyLocationUpdated(LocationDTO locationDTO) {
        if (isImportMode()) return;
        messagingTemplate.convertAndSend("/topic/locations/updated", locationDTO);
        notifyPersonsRefresh();
        notifyWorkersRefresh();
    }
    
    public void notifyLocationDeleted(Long locationId) {
        if (isImportMode()) return;
        messagingTemplate.convertAndSend("/topic/locations/deleted", locationId);
        notifyPersonsRefresh();
        notifyWorkersRefresh();
    }
    
    public void notifyAddressCreated(AddressDTO addressDTO) {
        if (isImportMode()) return;
        messagingTemplate.convertAndSend("/topic/addresses/created", addressDTO);
    }
    
    public void notifyAddressUpdated(AddressDTO addressDTO) {
        if (isImportMode()) return;
        messagingTemplate.convertAndSend("/topic/addresses/updated", addressDTO);
        notifyOrganizationsRefresh();
        notifyWorkersRefresh();
    }
    
    public void notifyAddressDeleted(Long addressId) {
        if (isImportMode()) return;
        messagingTemplate.convertAndSend("/topic/addresses/deleted", addressId);
        notifyOrganizationsRefresh();
        notifyWorkersRefresh();
    }
    
    public void notifyPersonCreated(PersonDTO personDTO) {
        if (isImportMode()) return;
        messagingTemplate.convertAndSend("/topic/persons/created", personDTO);
    }
    
    public void notifyPersonUpdated(PersonDTO personDTO) {
        if (isImportMode()) return;
        messagingTemplate.convertAndSend("/topic/persons/updated", personDTO);
        notifyWorkersRefresh();
    }
    
    public void notifyPersonDeleted(Long personId) {
        if (isImportMode()) return;
        messagingTemplate.convertAndSend("/topic/persons/deleted", personId);
        notifyWorkersRefresh();
    }
    
    public void notifyPersonsRefresh() {
        if (isImportMode()) return;
        messagingTemplate.convertAndSend("/topic/persons/refresh", "refresh");
    }
    
    public void notifyOrganizationsRefresh() {
        if (isImportMode()) return;
        messagingTemplate.convertAndSend("/topic/organizations/refresh", "refresh");
    }
    
    public void notifyPersonsUpdated(List<PersonDTO> persons) {
        if (isImportMode()) return;
        persons.forEach(p -> messagingTemplate.convertAndSend("/topic/persons/updated", p));
        notifyWorkersRefresh();
    }
    
    public void notifyOrganizationsUpdated(List<OrganizationDTO> orgs) {
        if (isImportMode()) return;
        orgs.forEach(o -> messagingTemplate.convertAndSend("/topic/organizations/updated", o));
        notifyWorkersRefresh();
    }
    
    public void notifyWorkersUpdated(List<WorkerDTO> workers) {
        if (isImportMode()) return;
        workers.forEach(w -> messagingTemplate.convertAndSend("/topic/workers/updated", w));
    }
}
