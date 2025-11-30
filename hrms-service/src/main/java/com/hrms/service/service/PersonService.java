package com.hrms.service.service;

import com.hrms.core.mapper.LocationMapper;
import com.hrms.core.mapper.PersonMapper;
import com.hrms.core.model.dto.FilterDTO;
import com.hrms.core.model.dto.PersonDTO;
import com.hrms.core.model.entity.Location;
import com.hrms.core.model.entity.Person;
import com.hrms.service.repository.LocationRepository;
import com.hrms.service.repository.PersonRepository;
import com.hrms.service.repository.WorkerRepository;
import com.hrms.service.specification.PersonSpecification;
import lombok.RequiredArgsConstructor;
import one.util.streamex.StreamEx;
import org.apache.commons.collections4.MapUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PersonService {
    
    private final PersonRepository personRepository;
    private final PersonMapper personMapper;
    private final WorkerRepository workerRepository;
    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;
    private final NotificationService notificationService;

    public List<PersonDTO> findAll() {
        return StreamEx.of(personRepository.findAll()).map(personMapper::toDTO).toList();
    }

    public Page<PersonDTO> findAll(Pageable pageable) {
        return personRepository.findAll(pageable).map(personMapper::toDTO);
    }

    public Page<PersonDTO> findByFilters(List<FilterDTO> filters, Pageable pageable) {
        return personRepository.findAll(PersonSpecification.filterBy(filters), pageable).map(personMapper::toDTO);
    }

    public PersonDTO findById(Long id) {
        return personRepository.findById(id)
            .map(personMapper::toDTO)
            .orElseThrow(() -> new RuntimeException("Персона не найдена"));
    }
    
    public List<PersonDTO> findByIds(List<Long> ids) {
        return StreamEx.of(personRepository.findAllById(ids)).map(personMapper::toDTO).toList();
    }

    @Transactional
    public PersonDTO create(PersonDTO personDTO) {
        var person = personMapper.toEntity(personDTO);
        person.setLocation(resolveLocation(personDTO));
        return personMapper.toDTO(personRepository.save(person));
    }

    @Transactional
    public PersonDTO update(Long id, PersonDTO personDTO) {
        personRepository.findById(id).orElseThrow(() -> new RuntimeException("Персона не найдена"));
        var person = personMapper.toEntity(personDTO.withId(id));
        person.setLocation(resolveLocation(personDTO));
        return personMapper.toDTO(personRepository.save(person));
    }
    
    private Location resolveLocation(PersonDTO dto) {
        if (dto.getLocation() == null) {
            return null;
        }
        var locDto = dto.getLocation();
        if (locDto.getId() != null) {
            var existing = locationRepository.findById(locDto.getId())
                .orElseThrow(() -> new RuntimeException("Локация не найдена"));
            var updated = existing.toBuilder()
                .x(locDto.getX())
                .y(locDto.getY())
                .z(locDto.getZ() != null ? locDto.getZ() : existing.getZ())
                .name(locDto.getName())
                .build();
            var saved = locationRepository.save(updated);
            notificationService.notifyLocationUpdated(locationMapper.toDTO(saved));
            return saved;
        }
        if (locDto.getZ() == null) {
            return null;
        }
        var created = locationRepository.save(Location.builder()
            .x(locDto.getX())
            .y(locDto.getY())
            .z(locDto.getZ())
            .name(locDto.getName())
            .build());
        notificationService.notifyLocationCreated(locationMapper.toDTO(created));
        return created;
    }

    @Transactional
    public List<PersonDTO> batchUpdateLocation(List<PersonDTO> dtos) {
        var ids = StreamEx.of(dtos).map(PersonDTO::getId).toList();
        var persons = personRepository.findAllById(ids);
        var dtoMap = StreamEx.of(dtos).toMap(PersonDTO::getId, d -> d);
        var updated = StreamEx.of(persons)
            .map(p -> {
                var dto = dtoMap.get(p.getId());
                if (dto == null || dto.getLocation() == null) {
                    return p.withLocation(null);
                }
                var loc = p.getLocation();
                if (loc == null) {
                    loc = new Location();
                }
                return p.withLocation(loc.toBuilder()
                    .x(dto.getLocation().getX())
                    .y(dto.getLocation().getY())
                    .z(dto.getLocation().getZ())
                    .name(dto.getLocation().getName())
                    .build());
            })
            .toList();
        return StreamEx.of(personRepository.saveAll(updated)).map(personMapper::toDTO).toList();
    }

    public List<Long> getRelatedWorkerIds(Long personId) {
        return workerRepository.findIdsByPersonId(personId);
    }

    @Transactional
    public List<Long> deleteWithReplacements(Long id, Map<Long, Long> workerToPersonMap) {
        var toDelete = personRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Персона не найдена"));
        List<Long> updatedWorkerIds = List.of();
        if (MapUtils.isNotEmpty(workerToPersonMap)) {
            var workers = workerRepository.findAllById(workerToPersonMap.keySet());
            var persons = StreamEx.of(personRepository.findAllById(workerToPersonMap.values())).toMap(Person::getId, p -> p);
            var updated = StreamEx.of(workers)
                .map(w -> {
                    var newPersonId = workerToPersonMap.get(w.getId());
                    return newPersonId != null && persons.containsKey(newPersonId) ? w.withPerson(persons.get(newPersonId)) : w;
                })
                .toList();
            workerRepository.saveAll(updated);
            updatedWorkerIds = StreamEx.of(updated).map(w -> w.getId()).toList();
        }
        personRepository.delete(toDelete);
        return updatedWorkerIds;
    }

    @Transactional
    public void delete(Long id) {
        var relatedWorkers = workerRepository.findIdsByPersonId(id);
        if (!relatedWorkers.isEmpty()) {
            throw new RuntimeException("Персона работает в " + relatedWorkers.size() + " должностях. Сначала назначьте замену.");
        }
        personRepository.deleteById(id);
    }
    
    // public long countPersons() {
    //     return personRepository.count();
    // }
}
