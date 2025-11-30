package com.hrms.api.controller;

import com.hrms.api.facade.LocationFacade;
import com.hrms.api.request.CreateLocationRequest;
import com.hrms.api.request.UpdateLocationRequest;
import com.hrms.api.request.DeleteLocationRequest;
import com.hrms.api.response.LocationResponse;
import com.hrms.core.model.dto.FilterDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {
    private final LocationFacade locationFacade;
    
    @GetMapping
    public ResponseEntity<?> getAll(Pageable pageable) {
        if (pageable.isPaged()) {
            return ResponseEntity.ok(locationFacade.getAllLocations(pageable));
        }
        return ResponseEntity.ok(locationFacade.getAllLocations());
    }
    
    @PostMapping("/filter")
    public ResponseEntity<Page<LocationResponse>> filter(@RequestBody List<FilterDTO> filters, Pageable pageable) {
        return ResponseEntity.ok(locationFacade.getFilteredLocations(filters, pageable));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<LocationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(locationFacade.getLocationById(id));
    }
    
    @GetMapping("/{id}/related")
    public ResponseEntity<List<Long>> getRelatedPersons(@PathVariable Long id) {
        return ResponseEntity.ok(locationFacade.getRelatedPersonIds(id));
    }
    
    @PostMapping
    public ResponseEntity<LocationResponse> create(@Valid @RequestBody CreateLocationRequest request) {
        return ResponseEntity.ok(locationFacade.createLocation(request));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<LocationResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateLocationRequest request) {
        return ResponseEntity.ok(locationFacade.updateLocation(id, request));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestBody(required = false) DeleteLocationRequest request) {
        locationFacade.deleteWithReplacements(id, request);
        return ResponseEntity.noContent().build();
    }
}

