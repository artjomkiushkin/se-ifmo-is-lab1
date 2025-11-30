package com.hrms.api.facade;

import com.hrms.api.mapper.OrganizationFacadeMapper;
import com.hrms.api.request.BatchUpdateOrganizationRequest;
import com.hrms.api.request.CreateOrganizationRequest;
import com.hrms.api.request.DeleteOrganizationRequest;
import com.hrms.api.request.UpdateOrganizationRequest;
import com.hrms.api.response.OrganizationResponse;
import com.hrms.core.model.dto.FilterDTO;
import com.hrms.service.service.NotificationService;
import com.hrms.service.service.OrganizationService;
import com.hrms.service.service.WorkerService;
import lombok.RequiredArgsConstructor;
import one.util.streamex.StreamEx;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OrganizationFacade {
    private final OrganizationService organizationService;
    private final OrganizationFacadeMapper mapper;
    private final NotificationService notificationService;
    private final WorkerService workerService;
    
    public List<OrganizationResponse> getAllOrganizations() {
        var dtos = organizationService.findAll();
        return mapper.toResponseList(dtos);
    }
    
    public Page<OrganizationResponse> getAllOrganizations(Pageable pageable) {
        var dtoPage = organizationService.findAll(pageable);
        return dtoPage.map(mapper::toResponse);
    }
    
    public Page<OrganizationResponse> getFilteredOrganizations(List<FilterDTO> filters, Pageable pageable) {
        var dtoPage = organizationService.findByFilters(filters, pageable);
        return dtoPage.map(mapper::toResponse);
    }
    
    public OrganizationResponse getOrganizationById(Long id) {
        var dto = organizationService.findById(id);
        return mapper.toResponse(dto);
    }
    
    public OrganizationResponse createOrganization(CreateOrganizationRequest request) {
        var organizationDTO = mapper.toDTO(request);
        var created = organizationService.create(organizationDTO);
        notificationService.notifyOrganizationCreated(created);
        return mapper.toResponse(created);
    }
    
    public OrganizationResponse updateOrganization(Long id, UpdateOrganizationRequest request) {
        var organizationDTO = mapper.toDTO(request);
        var updated = organizationService.update(id, organizationDTO);
        notificationService.notifyOrganizationUpdated(updated);
        return mapper.toResponse(updated);
    }
    
    public List<Long> getRelatedWorkerIds(Long id) {
        return organizationService.getRelatedWorkerIds(id);
    }
    
    public void deleteOrganization(Long id) {
        organizationService.delete(id);
        notificationService.notifyOrganizationDeleted(id);
    }
    
    public void deleteWithReplacements(Long id, DeleteOrganizationRequest request) {
        Map<Long, Long> replacementMap = request != null && request.getReplacements() != null
            ? StreamEx.of(request.getReplacements()).toMap(
                DeleteOrganizationRequest.WorkerReplacement::getWorkerId,
                DeleteOrganizationRequest.WorkerReplacement::getNewOrganizationId)
            : Map.of();
        var updatedWorkerIds = organizationService.deleteWithReplacements(id, replacementMap);
        notificationService.notifyOrganizationDeleted(id);
        if (!updatedWorkerIds.isEmpty()) {
            var updatedWorkers = workerService.findByIds(updatedWorkerIds);
            notificationService.notifyWorkersUpdated(updatedWorkers);
        }
    }
    
    public List<OrganizationResponse> batchUpdateOrganizations(List<BatchUpdateOrganizationRequest> requests) {
        var orgIds = requests.stream().map(BatchUpdateOrganizationRequest::getId).toList();
        var addressIds = requests.stream().map(BatchUpdateOrganizationRequest::getOfficialAddressId).toList();
        var updated = organizationService.batchUpdateAddress(orgIds, addressIds);
        notificationService.notifyOrganizationsRefresh();
        return mapper.toResponseList(updated);
    }
    
    public void indexSalaries(Long organizationId, double coefficient) {
        organizationService.indexSalaries(organizationId, coefficient);
        notificationService.notifyWorkersRefresh();
    }
}
