package com.hrms.api.facade;

import com.hrms.api.mapper.PersonFacadeMapper;
import com.hrms.api.request.BatchUpdatePersonRequest;
import com.hrms.api.request.CreatePersonRequest;
import com.hrms.api.request.DeletePersonRequest;
import com.hrms.api.request.UpdatePersonRequest;
import com.hrms.api.response.PersonResponse;
import com.hrms.core.model.dto.FilterDTO;
import com.hrms.service.service.NotificationService;
import com.hrms.service.service.PersonService;
import com.hrms.service.service.WorkerService;
import lombok.RequiredArgsConstructor;
import one.util.streamex.StreamEx;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PersonFacade {
    private final PersonService personService;
    private final PersonFacadeMapper mapper;
    private final NotificationService notificationService;
    private final WorkerService workerService;
    
    public List<PersonResponse> getAllPersons() {
        var dtos = personService.findAll();
        return mapper.toResponseList(dtos);
    }
    
    public Page<PersonResponse> getAllPersons(Pageable pageable) {
        var dtoPage = personService.findAll(pageable);
        return dtoPage.map(mapper::toResponse);
    }
    
    public Page<PersonResponse> getFilteredPersons(List<FilterDTO> filters, Pageable pageable) {
        var dtoPage = personService.findByFilters(filters, pageable);
        return dtoPage.map(mapper::toResponse);
    }
    
    public PersonResponse getPersonById(Long id) {
        var dto = personService.findById(id);
        return mapper.toResponse(dto);
    }
    
    public PersonResponse createPerson(CreatePersonRequest request) {
        var personDTO = mapper.toDTO(request);
        var created = personService.create(personDTO);
        notificationService.notifyPersonCreated(created);
        return mapper.toResponse(created);
    }
    
    public PersonResponse updatePerson(Long id, UpdatePersonRequest request) {
        var personDTO = mapper.toDTO(request);
        var updated = personService.update(id, personDTO);
        notificationService.notifyPersonUpdated(updated);
        return mapper.toResponse(updated);
    }
    
    @Transactional
    public List<PersonResponse> batchUpdatePersons(List<BatchUpdatePersonRequest> requests) {
        var dtos = requests.stream()
            .map(mapper::toDTO)
            .toList();
        var updated = personService.batchUpdateLocation(dtos);
        return mapper.toResponseList(updated);
    }
    
    public List<Long> getRelatedWorkerIds(Long id) {
        return personService.getRelatedWorkerIds(id);
    }
    
    public void deletePerson(Long id) {
        personService.delete(id);
        notificationService.notifyPersonDeleted(id);
    }
    
    public void deleteWithReplacements(Long id, DeletePersonRequest request) {
        Map<Long, Long> replacementMap = request != null && request.getReplacements() != null
            ? StreamEx.of(request.getReplacements()).toMap(
                DeletePersonRequest.WorkerReplacement::getWorkerId,
                DeletePersonRequest.WorkerReplacement::getNewPersonId)
            : Map.of();
        var updatedWorkerIds = personService.deleteWithReplacements(id, replacementMap);
        notificationService.notifyPersonDeleted(id);
        if (!updatedWorkerIds.isEmpty()) {
            var updatedWorkers = workerService.findByIds(updatedWorkerIds);
            notificationService.notifyWorkersUpdated(updatedWorkers);
        }
    }
}
