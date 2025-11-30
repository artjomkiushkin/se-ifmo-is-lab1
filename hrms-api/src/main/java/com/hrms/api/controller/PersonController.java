package com.hrms.api.controller;

import com.hrms.api.facade.PersonFacade;
import com.hrms.api.request.BatchUpdatePersonRequest;
import com.hrms.api.request.CreatePersonRequest;
import com.hrms.api.request.UpdatePersonRequest;
import com.hrms.api.request.DeletePersonRequest;
import com.hrms.api.response.PersonResponse;
import com.hrms.core.model.dto.FilterDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/persons")
@RequiredArgsConstructor
public class PersonController {
    private final PersonFacade personFacade;
    
    @GetMapping
    public ResponseEntity<?> getAll(Pageable pageable) {
        if (pageable.isPaged()) {
            return ResponseEntity.ok(personFacade.getAllPersons(pageable));
        }
        return ResponseEntity.ok(personFacade.getAllPersons());
    }
    
    @PostMapping("/filter")
    public ResponseEntity<Page<PersonResponse>> filter(@RequestBody List<FilterDTO> filters, Pageable pageable) {
        return ResponseEntity.ok(personFacade.getFilteredPersons(filters, pageable));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<PersonResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(personFacade.getPersonById(id));
    }
    
    @GetMapping("/{id}/related-workers")
    public ResponseEntity<List<Long>> getRelatedWorkers(@PathVariable Long id) {
        return ResponseEntity.ok(personFacade.getRelatedWorkerIds(id));
    }
    
    @PostMapping
    public ResponseEntity<PersonResponse> create(@Valid @RequestBody CreatePersonRequest request) {
        return ResponseEntity.ok(personFacade.createPerson(request));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<PersonResponse> update(@PathVariable Long id, @Valid @RequestBody UpdatePersonRequest request) {
        return ResponseEntity.ok(personFacade.updatePerson(id, request));
    }
    
    @PatchMapping("/batch")
    public ResponseEntity<List<PersonResponse>> batchUpdate(@Valid @RequestBody List<BatchUpdatePersonRequest> requests) {
        return ResponseEntity.ok(personFacade.batchUpdatePersons(requests));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestBody(required = false) DeletePersonRequest request) {
        personFacade.deleteWithReplacements(id, request);
        return ResponseEntity.noContent().build();
    }
}

