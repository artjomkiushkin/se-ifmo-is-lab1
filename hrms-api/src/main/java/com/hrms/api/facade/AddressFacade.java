package com.hrms.api.facade;

import com.hrms.api.mapper.AddressFacadeMapper;
import com.hrms.api.request.CreateAddressRequest;
import com.hrms.api.request.DeleteAddressRequest;
import com.hrms.api.request.UpdateAddressRequest;
import com.hrms.api.response.AddressResponse;
import com.hrms.core.model.dto.FilterDTO;
import com.hrms.service.service.AddressService;
import com.hrms.service.service.NotificationService;
import com.hrms.service.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import one.util.streamex.StreamEx;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AddressFacade {
    private final AddressService addressService;
    private final AddressFacadeMapper mapper;
    private final NotificationService notificationService;
    private final OrganizationService organizationService;
    
    public List<AddressResponse> getAllAddresses() {
        var dtos = addressService.findAll();
        return mapper.toResponseList(dtos);
    }
    
    public Page<AddressResponse> getAllAddresses(Pageable pageable) {
        var dtoPage = addressService.findAll(pageable);
        return dtoPage.map(mapper::toResponse);
    }
    
    public Page<AddressResponse> getFilteredAddresses(List<FilterDTO> filters, Pageable pageable) {
        var dtoPage = addressService.findByFilters(filters, pageable);
        return dtoPage.map(mapper::toResponse);
    }
    
    public AddressResponse getAddressById(Long id) {
        var dto = addressService.findById(id);
        return mapper.toResponse(dto);
    }
    
    public AddressResponse createAddress(CreateAddressRequest request) {
        var dto = mapper.toDTO(request);
        var created = addressService.create(dto);
        notificationService.notifyAddressCreated(created);
        return mapper.toResponse(created);
    }
    
    public AddressResponse updateAddress(Long id, UpdateAddressRequest request) {
        var dto = mapper.toDTO(request);
        var updated = addressService.update(id, dto);
        notificationService.notifyAddressUpdated(updated);
        return mapper.toResponse(updated);
    }
    
    public List<Long> getRelatedOrganizationIds(Long id) {
        return addressService.getRelatedOrganizationIds(id);
    }
    
    public void deleteAddress(Long id, Long replacementId) {
        addressService.delete(id, replacementId);
        notificationService.notifyAddressDeleted(id);
    }
    
    public void deleteWithReplacements(Long id, DeleteAddressRequest request) {
        Map<Long, Long> replacementMap = request != null && request.getReplacements() != null
            ? StreamEx.of(request.getReplacements()).toMap(
                DeleteAddressRequest.OrganizationReplacement::getOrganizationId,
                DeleteAddressRequest.OrganizationReplacement::getNewAddressId)
            : Map.of();
        var updatedOrgIds = addressService.deleteWithReplacements(id, replacementMap);
        notificationService.notifyAddressDeleted(id);
        if (!updatedOrgIds.isEmpty()) {
            var updatedOrgs = organizationService.findByIds(updatedOrgIds);
            notificationService.notifyOrganizationsUpdated(updatedOrgs);
        }
    }
}
