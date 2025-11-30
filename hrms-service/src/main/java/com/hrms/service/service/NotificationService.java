package com.hrms.service.service;

import com.hrms.core.model.dto.AddressDTO;
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
    
    // private static final String TOPIC_PREFIX = "/topic/";
    
    public void notifyWorkerCreated(WorkerDTO workerDTO) {
        messagingTemplate.convertAndSend("/topic/workers/created", workerDTO);
    }
    
    public void notifyWorkerUpdated(WorkerDTO workerDTO) {
        messagingTemplate.convertAndSend("/topic/workers/updated", workerDTO);
    }
    
    public void notifyWorkerDeleted(Long workerId) {
        messagingTemplate.convertAndSend("/topic/workers/deleted", workerId);
    }
    
    public void notifyWorkersRefresh() {
        messagingTemplate.convertAndSend("/topic/workers/refresh", "refresh");
    }
    
    public void notifyOrganizationCreated(OrganizationDTO dto) {
        messagingTemplate.convertAndSend("/topic/organizations/created", dto);
    }
    
    public void notifyOrganizationUpdated(OrganizationDTO dto) {
        messagingTemplate.convertAndSend("/topic/organizations/updated", dto);
        notifyWorkersRefresh();
    }
    
    public void notifyOrganizationDeleted(Long id) {
        messagingTemplate.convertAndSend("/topic/organizations/deleted", id);
        notifyWorkersRefresh();
    }
    
    public void notifyLocationCreated(LocationDTO locationDTO) {
        messagingTemplate.convertAndSend("/topic/locations/created", locationDTO);
    }
    
    public void notifyLocationUpdated(LocationDTO locationDTO) {
        messagingTemplate.convertAndSend("/topic/locations/updated", locationDTO);
        notifyPersonsRefresh();
        notifyWorkersRefresh();
    }
    
    public void notifyLocationDeleted(Long locationId) {
        messagingTemplate.convertAndSend("/topic/locations/deleted", locationId);
        notifyPersonsRefresh();
        notifyWorkersRefresh();
    }
    
    public void notifyAddressCreated(AddressDTO addressDTO) {
        messagingTemplate.convertAndSend("/topic/addresses/created", addressDTO);
    }
    
    public void notifyAddressUpdated(AddressDTO addressDTO) {
        messagingTemplate.convertAndSend("/topic/addresses/updated", addressDTO);
        notifyOrganizationsRefresh();
        notifyWorkersRefresh();
    }
    
    public void notifyAddressDeleted(Long addressId) {
        messagingTemplate.convertAndSend("/topic/addresses/deleted", addressId);
        notifyOrganizationsRefresh();
        notifyWorkersRefresh();
    }
    
    public void notifyPersonCreated(PersonDTO personDTO) {
        messagingTemplate.convertAndSend("/topic/persons/created", personDTO);
    }
    
    public void notifyPersonUpdated(PersonDTO personDTO) {
        messagingTemplate.convertAndSend("/topic/persons/updated", personDTO);
        notifyWorkersRefresh();
    }
    
    public void notifyPersonDeleted(Long personId) {
        messagingTemplate.convertAndSend("/topic/persons/deleted", personId);
        notifyWorkersRefresh();
    }
    
    public void notifyPersonsRefresh() {
        messagingTemplate.convertAndSend("/topic/persons/refresh", "refresh");
    }
    
    public void notifyOrganizationsRefresh() {
        messagingTemplate.convertAndSend("/topic/organizations/refresh", "refresh");
    }
    
    public void notifyPersonsUpdated(List<PersonDTO> persons) {
        persons.forEach(p -> messagingTemplate.convertAndSend("/topic/persons/updated", p));
        notifyWorkersRefresh();
    }
    
    public void notifyOrganizationsUpdated(List<OrganizationDTO> orgs) {
        orgs.forEach(o -> messagingTemplate.convertAndSend("/topic/organizations/updated", o));
        notifyWorkersRefresh();
    }
    
    public void notifyWorkersUpdated(List<WorkerDTO> workers) {
        workers.forEach(w -> messagingTemplate.convertAndSend("/topic/workers/updated", w));
    }
}
