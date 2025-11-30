package com.hrms.api.facade;

import com.hrms.api.mapper.LocationFacadeMapper;
import com.hrms.api.request.CreateLocationRequest;
import com.hrms.api.request.DeleteLocationRequest;
import com.hrms.api.request.UpdateLocationRequest;
import com.hrms.api.response.LocationResponse;
import com.hrms.core.model.dto.FilterDTO;
import com.hrms.service.service.LocationService;
import com.hrms.service.service.NotificationService;
import com.hrms.service.service.PersonService;
import lombok.RequiredArgsConstructor;
import one.util.streamex.StreamEx;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class LocationFacade {
    private final LocationService locationService;
    private final LocationFacadeMapper mapper;
    private final NotificationService notificationService;
    private final PersonService personService;
    
    public List<LocationResponse> getAllLocations() {
        var dtos = locationService.findAll();
        return mapper.toResponseList(dtos);
    }
    
    public Page<LocationResponse> getAllLocations(Pageable pageable) {
        var dtoPage = locationService.findAll(pageable);
        return dtoPage.map(mapper::toResponse);
    }
    
    public Page<LocationResponse> getFilteredLocations(List<FilterDTO> filters, Pageable pageable) {
        var dtoPage = locationService.findByFilters(filters, pageable);
        return dtoPage.map(mapper::toResponse);
    }
    
    public LocationResponse getLocationById(Long id) {
        var dto = locationService.findById(id);
        return mapper.toResponse(dto);
    }
    
    public LocationResponse createLocation(CreateLocationRequest request) {
        var dto = mapper.toDTO(request);
        var created = locationService.create(dto);
        notificationService.notifyLocationCreated(created);
        return mapper.toResponse(created);
    }
    
    public LocationResponse updateLocation(Long id, UpdateLocationRequest request) {
        var dto = mapper.toDTO(request);
        var updated = locationService.update(id, dto);
        notificationService.notifyLocationUpdated(updated);
        return mapper.toResponse(updated);
    }
    
    public List<Long> getRelatedPersonIds(Long id) {
        return locationService.getRelatedPersonIds(id);
    }
    
    public void deleteLocation(Long id, Long replacementId) {
        locationService.delete(id, replacementId);
        notificationService.notifyLocationDeleted(id);
    }
    
    public void deleteWithReplacements(Long id, DeleteLocationRequest request) {
        Map<Long, Long> replacementMap = request != null && request.getReplacements() != null
            ? StreamEx.of(request.getReplacements()).toMap(
                DeleteLocationRequest.PersonReplacement::getPersonId,
                DeleteLocationRequest.PersonReplacement::getNewLocationId)
            : Map.of();
        var updatedPersonIds = locationService.deleteWithReplacements(id, replacementMap);
        notificationService.notifyLocationDeleted(id);
        if (!updatedPersonIds.isEmpty()) {
            var updatedPersons = personService.findByIds(updatedPersonIds);
            notificationService.notifyPersonsUpdated(updatedPersons);
        }
    }
}
