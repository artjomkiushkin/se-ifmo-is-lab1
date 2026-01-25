package com.hrms.service.service;

import com.hrms.core.mapper.LocationMapper;
import com.hrms.core.model.dto.FilterDTO;
import com.hrms.core.model.dto.LocationDTO;
import com.hrms.core.model.entity.Location;
import com.hrms.core.model.entity.Person;
import com.hrms.service.exception.EntityNotFoundException;
import com.hrms.service.repository.LocationRepository;
import com.hrms.service.exception.EntityNotFoundException;
import com.hrms.service.repository.PersonRepository;
import com.hrms.service.specification.LocationSpecification;
import lombok.RequiredArgsConstructor;
import one.util.streamex.StreamEx;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class LocationService {
    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;
    private final PersonRepository personRepository;

    public List<LocationDTO> findAll() {
        return StreamEx.of(locationRepository.findAll()).map(locationMapper::toDTO).toList();
    }

    public Page<LocationDTO> findAll(Pageable pageable) {
        return locationRepository.findAll(pageable).map(locationMapper::toDTO);
    }

    public Page<LocationDTO> findByFilters(List<FilterDTO> filters, Pageable pageable) {
        return locationRepository.findAll(LocationSpecification.filterBy(filters), pageable).map(locationMapper::toDTO);
    }

    public LocationDTO findById(Long id) {
        return locationRepository.findById(id)
            .map(locationMapper::toDTO)
            .orElseThrow(() -> new EntityNotFoundException("Локация не найдена"));
    }

    @Transactional
    public LocationDTO create(LocationDTO locationDTO) {
        var location = locationMapper.toEntity(locationDTO);
        return locationMapper.toDTO(locationRepository.save(location));
    }

    @Transactional
    public LocationDTO update(Long id, LocationDTO locationDTO) {
        var existing = locationRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Локация не найдена"));
        var updated = existing.toBuilder()
            .x(locationDTO.getX())
            .y(locationDTO.getY())
            .z(locationDTO.getZ())
            .name(locationDTO.getName())
            .build();
        return locationMapper.toDTO(locationRepository.save(updated));
    }

    public List<Long> getRelatedPersonIds(Long locationId) {
        return personRepository.findIdsByLocationId(locationId);
    }

    @Transactional
    public List<Long> deleteWithReplacements(Long id, Map<Long, Long> personToLocationMap) {
        var toDelete = locationRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Локация не найдена"));
        List<Long> updatedPersonIds = List.of();
        if (MapUtils.isNotEmpty(personToLocationMap)) {
            var persons = personRepository.findAllById(personToLocationMap.keySet());
            var locationIds = StreamEx.of(personToLocationMap.values()).filter(Objects::nonNull).toList();
            var locations = StreamEx.of(locationRepository.findAllById(locationIds)).toMap(Location::getId, l -> l);
            var updated = StreamEx.of(persons)
                .map(p -> {
                    var newLocId = personToLocationMap.get(p.getId());
                    if (newLocId == null) {
                        return p.withLocation(null);
                    }
                    return locations.containsKey(newLocId) ? p.withLocation(locations.get(newLocId)) : p;
                })
                .toList();
            personRepository.saveAll(updated);
            updatedPersonIds = StreamEx.of(updated).map(Person::getId).toList();
        }
        locationRepository.delete(toDelete);
        return updatedPersonIds;
    }

    @Transactional
    public void delete(Long id, Long replacementId) {
        var toDelete = locationRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Локация не найдена"));
        var relatedPersonIds = personRepository.findIdsByLocationId(id);
        if (!relatedPersonIds.isEmpty()) {
            if (replacementId == null) {
                throw new RuntimeException("Локация используется в " + relatedPersonIds.size() + " персонах. Укажите замену.");
            }
            var replacement = locationRepository.findById(replacementId)
                .orElseThrow(() -> new EntityNotFoundException("Локация для замены не найдена"));
            var persons = personRepository.findAllById(relatedPersonIds);
            var updated = StreamEx.of(persons).map(p -> p.withLocation(replacement)).toList();
            personRepository.saveAll(updated);
        }
        locationRepository.delete(toDelete);
    }
}
