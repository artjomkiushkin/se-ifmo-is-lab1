package com.hrms.api.controller;

import com.hrms.api.facade.AddressFacade;
import com.hrms.api.request.CreateAddressRequest;
import com.hrms.api.request.UpdateAddressRequest;
import com.hrms.api.request.DeleteAddressRequest;
import com.hrms.api.response.AddressResponse;
import com.hrms.core.model.dto.FilterDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {
    private final AddressFacade addressFacade;
    
    @GetMapping
    public ResponseEntity<?> getAll(Pageable pageable) {
        if (pageable.isPaged()) {
            return ResponseEntity.ok(addressFacade.getAllAddresses(pageable));
        }
        return ResponseEntity.ok(addressFacade.getAllAddresses());
    }
    
    @PostMapping("/filter")
    public ResponseEntity<Page<AddressResponse>> filter(@RequestBody List<FilterDTO> filters, Pageable pageable) {
        return ResponseEntity.ok(addressFacade.getFilteredAddresses(filters, pageable));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<AddressResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(addressFacade.getAddressById(id));
    }
    
    @GetMapping("/{id}/related")
    public ResponseEntity<List<Long>> getRelatedOrganizations(@PathVariable Long id) {
        return ResponseEntity.ok(addressFacade.getRelatedOrganizationIds(id));
    }
    
    @PostMapping
    public ResponseEntity<AddressResponse> create(@Valid @RequestBody CreateAddressRequest request) {
        return ResponseEntity.ok(addressFacade.createAddress(request));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<AddressResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateAddressRequest request) {
        return ResponseEntity.ok(addressFacade.updateAddress(id, request));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestBody(required = false) DeleteAddressRequest request) {
        addressFacade.deleteWithReplacements(id, request);
        return ResponseEntity.noContent().build();
    }
}

