package com.hrms.api.controller;

import com.hrms.api.facade.OrganizationFacade;
import com.hrms.api.request.BatchUpdateOrganizationRequest;
import com.hrms.api.request.CreateOrganizationRequest;
import com.hrms.api.request.IndexSalaryRequest;
import com.hrms.api.request.UpdateOrganizationRequest;
import com.hrms.api.request.DeleteOrganizationRequest;
import com.hrms.api.response.OrganizationResponse;
import com.hrms.core.model.dto.FilterDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganizationController {
    private final OrganizationFacade organizationFacade;
    
    @GetMapping
    public ResponseEntity<?> getAll(Pageable pageable) {
        if (pageable.isPaged()) {
            return ResponseEntity.ok(organizationFacade.getAllOrganizations(pageable));
        }
        return ResponseEntity.ok(organizationFacade.getAllOrganizations());
    }
    
    @PostMapping("/filter")
    public ResponseEntity<Page<OrganizationResponse>> filter(@RequestBody List<FilterDTO> filters, Pageable pageable) {
        return ResponseEntity.ok(organizationFacade.getFilteredOrganizations(filters, pageable));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<OrganizationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(organizationFacade.getOrganizationById(id));
    }
    
    @GetMapping("/{id}/related-workers")
    public ResponseEntity<List<Long>> getRelatedWorkers(@PathVariable Long id) {
        return ResponseEntity.ok(organizationFacade.getRelatedWorkerIds(id));
    }
    
    @PostMapping
    public ResponseEntity<OrganizationResponse> create(@Valid @RequestBody CreateOrganizationRequest request) {
        return ResponseEntity.ok(organizationFacade.createOrganization(request));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<OrganizationResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateOrganizationRequest request) {
        return ResponseEntity.ok(organizationFacade.updateOrganization(id, request));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestBody(required = false) DeleteOrganizationRequest request) {
        organizationFacade.deleteWithReplacements(id, request);
        return ResponseEntity.noContent().build();
    }
    
    @PatchMapping("/batch")
    public ResponseEntity<List<OrganizationResponse>> batchUpdate(@Valid @RequestBody List<BatchUpdateOrganizationRequest> requests) {
        return ResponseEntity.ok(organizationFacade.batchUpdateOrganizations(requests));
    }
    
    @PutMapping("/{id}/workers/salary/indexation")
    public ResponseEntity<Void> indexSalaries(@PathVariable Long id, @RequestBody IndexSalaryRequest request) {
        if (request.getCoefficient() <= 1) {
            throw new IllegalArgumentException("Коэффициент должен быть больше 1");
        }
        organizationFacade.indexSalaries(id, request.getCoefficient());
        return ResponseEntity.noContent().build();
    }
}

