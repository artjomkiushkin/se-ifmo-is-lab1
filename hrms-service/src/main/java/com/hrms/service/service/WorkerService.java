package com.hrms.service.service;

import com.hrms.core.mapper.AddressMapper;
import com.hrms.core.mapper.LocationMapper;
import com.hrms.core.mapper.OrganizationMapper;
import com.hrms.core.mapper.PersonMapper;
import com.hrms.core.mapper.WorkerMapper;
import com.hrms.core.model.dto.*;
import com.hrms.core.model.entity.*;
import com.hrms.core.model.enums.Color;
import com.hrms.core.model.enums.Country;
import com.hrms.core.model.enums.OrganizationType;
import com.hrms.core.model.enums.Position;
import com.hrms.service.repository.*;
import com.hrms.service.specification.WorkerSpecification;
import lombok.RequiredArgsConstructor;
import one.util.streamex.StreamEx;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class WorkerService {
    private static final Logger log = Logger.getLogger(WorkerService.class.getName());
    private final WorkerRepository workerRepository;
    private final WorkerMapper workerMapper;
    private final OrganizationRepository organizationRepository;
    private final PersonRepository personRepository;
    private final LocationRepository locationRepository;
    private final AddressRepository addressRepository;
    private final OrganizationMapper organizationMapper;
    private final PersonMapper personMapper;
    private final LocationMapper locationMapper;
    private final AddressMapper addressMapper;
    private final NotificationService notificationService;

    public Page<WorkerDTO> findAll(Pageable pageable) {
        return workerRepository.findAll(pageable).map(workerMapper::toDTO);
    }

    public Page<WorkerDTO> findByFilters(List<FilterDTO> filters, Pageable pageable) {
        return workerRepository.findAll(WorkerSpecification.filterBy(filters), pageable).map(workerMapper::toDTO);
    }

    public WorkerDTO findById(Long id) {
        return workerRepository.findById(id)
            .map(workerMapper::toDTO)
            .orElseThrow(() -> new RuntimeException("Работник не найден"));
    }
    
    public List<WorkerDTO> findByIds(List<Long> ids) {
        return StreamEx.of(workerRepository.findAllById(ids)).map(workerMapper::toDTO).toList();
    }

    @Transactional
    public WorkerDTO create(WorkerDTO workerDTO) {
        var worker = workerMapper.toEntity(workerDTO);
        if (workerDTO.getOrganization() != null && workerDTO.getOrganization().getId() != null) {
            worker.setOrganization(findOrganization(workerDTO.getOrganization().getId()));
        }
        if (workerDTO.getPerson() != null && workerDTO.getPerson().getId() != null) {
            worker.setPerson(findPerson(workerDTO.getPerson().getId()));
        }
        return workerMapper.toDTO(workerRepository.save(worker));
    }

    @Transactional
    public WorkerDTO createWithNested(CreateWorkerDTO dto) {
        // log.info("Creating worker: " + dto.getPosition());
        var endDate = StringUtils.isNotBlank(dto.getEndDate()) ? ZonedDateTime.parse(dto.getEndDate()) : null;
        validateDates(dto.getStartDate(), endDate);
        var worker = Worker.builder()
            .coordinates(Coordinates.builder().x(dto.getCoordinatesX()).y(dto.getCoordinatesY()).build())
            .salary(dto.getSalary())
            .rating(dto.getRating())
            .startDate(dto.getStartDate())
            .position(Position.valueOf(dto.getPosition()))
            .endDate(endDate)
            .organization(resolveOrganization(dto))
            .person(resolvePerson(dto))
            .build();
        return workerMapper.toDTO(workerRepository.save(worker));
    }

    private Organization resolveOrganization(CreateWorkerDTO dto) {
        if (dto.getOrganizationId() != null) {
            return findOrganization(dto.getOrganizationId());
        }
        if (dto.getEditOrganization() != null) {
            return updateOrganization(dto.getEditOrganization());
        }
        if (dto.getNewOrganization() == null) {
            throw new RuntimeException("Укажите организацию или данные для создания новой");
        }
        return createOrganization(dto.getNewOrganization());
    }
    
    private Organization updateOrganization(OrganizationDTO edit) {
        var existing = findOrganization(edit.getId());
        var updated = existing.toBuilder()
            .fullName(edit.getFullName())
            .annualTurnover(edit.getAnnualTurnover())
            .rating(edit.getRating())
            .employeesCount(edit.getEmployeesCount())
            .type(edit.getType())
            .officialAddress(resolveAddressForEdit(edit))
            .build();
        var saved = organizationRepository.save(updated);
        notificationService.notifyOrganizationUpdated(organizationMapper.toDTO(saved));
        return saved;
    }
    
    private Address resolveAddressForEdit(OrganizationDTO edit) {
        if (edit.getOfficialAddress() == null) {
            return null;
        }
        var addr = edit.getOfficialAddress();
        if (addr.getId() != null && addr.getZipCode() != null) {
            var existing = findAddress(addr.getId());
            var saved = addressRepository.save(existing.withZipCode(addr.getZipCode()));
            notificationService.notifyAddressUpdated(addressMapper.toDTO(saved));
            return saved;
        }
        if (addr.getId() != null) {
            return findAddress(addr.getId());
        }
        if (addr.getZipCode() != null) {
            var created = addressRepository.save(Address.builder().zipCode(addr.getZipCode()).build());
            notificationService.notifyAddressCreated(addressMapper.toDTO(created));
            return created;
        }
        return null;
    }

    private Organization createOrganization(OrganizationDTO nested) {
        var org = Organization.builder()
            .fullName(nested.getFullName())
            .annualTurnover(nested.getAnnualTurnover())
            .rating(nested.getRating())
            .employeesCount(nested.getEmployeesCount())
            .type(nested.getType())
            .officialAddress(resolveAddress(nested))
            .build();
        var saved = organizationRepository.save(org);
        notificationService.notifyOrganizationCreated(organizationMapper.toDTO(saved));
        return saved;
    }

    private Address resolveAddress(OrganizationDTO nested) {
        if (nested.getOfficialAddress() == null) {
            return null;
        }
        if (nested.getOfficialAddress().getId() != null) {
            return findAddress(nested.getOfficialAddress().getId());
        }
        var created = addressRepository.save(Address.builder().zipCode(nested.getOfficialAddress().getZipCode()).build());
        notificationService.notifyAddressCreated(addressMapper.toDTO(created));
        return created;
    }

    private Person resolvePerson(CreateWorkerDTO dto) {
        if (dto.getPersonId() != null) {
            return findPerson(dto.getPersonId());
        }
        if (dto.getEditPerson() != null) {
            return updatePerson(dto.getEditPerson());
        }
        if (dto.getNewPerson() == null) {
            throw new RuntimeException("Укажите персону или данные для создания новой");
        }
        return createPerson(dto.getNewPerson());
    }
    
    private Person updatePerson(PersonDTO edit) {
        var existing = findPerson(edit.getId());
        var updated = existing.toBuilder()
            .name(edit.getName())
            .eyeColor(edit.getEyeColor())
            .hairColor(edit.getHairColor())
            .height(edit.getHeight())
            .nationality(edit.getNationality())
            .location(resolveLocationForEdit(edit))
            .build();
        var saved = personRepository.save(updated);
        notificationService.notifyPersonUpdated(personMapper.toDTO(saved));
        return saved;
    }
    
    private Location resolveLocationForEdit(PersonDTO edit) {
        if (edit.getLocation() == null) {
            return null;
        }
        var loc = edit.getLocation();
        if (loc.getId() != null && loc.getZ() != null) {
            var existing = findLocation(loc.getId());
            var updated = existing.toBuilder()
                .x(loc.getX())
                .y(loc.getY())
                .z(loc.getZ())
                .name(loc.getName())
                .build();
            var saved = locationRepository.save(updated);
            notificationService.notifyLocationUpdated(locationMapper.toDTO(saved));
            return saved;
        }
        if (loc.getId() != null) {
            return findLocation(loc.getId());
        }
        if (loc.getZ() != null) {
            var created = locationRepository.save(Location.builder().x(loc.getX()).y(loc.getY()).z(loc.getZ()).name(loc.getName()).build());
            notificationService.notifyLocationCreated(locationMapper.toDTO(created));
            return created;
        }
        return null;
    }

    private Person createPerson(PersonDTO nested) {
        var person = Person.builder()
            .name(nested.getName())
            .eyeColor(nested.getEyeColor())
            .hairColor(nested.getHairColor())
            .height(nested.getHeight())
            .nationality(nested.getNationality())
            .location(resolveLocation(nested))
            .build();
        var saved = personRepository.save(person);
        notificationService.notifyPersonCreated(personMapper.toDTO(saved));
        return saved;
    }

    private Location resolveLocation(PersonDTO nested) {
        if (nested.getLocation() == null) {
            return null;
        }
        if (nested.getLocation().getId() != null) {
            return findLocation(nested.getLocation().getId());
        }
        var loc = nested.getLocation();
        var created = locationRepository.save(Location.builder().x(loc.getX()).y(loc.getY()).z(loc.getZ()).name(loc.getName()).build());
        notificationService.notifyLocationCreated(locationMapper.toDTO(created));
        return created;
    }

    @Transactional
    public WorkerDTO update(Long id, WorkerDTO workerDTO) {
        validateDates(workerDTO.getStartDate(), workerDTO.getEndDate());
        var existing = workerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Работник не найден"));
        var updated = existing.toBuilder()
            .salary(workerDTO.getSalary())
            .rating(workerDTO.getRating())
            .position(workerDTO.getPosition())
            .startDate(workerDTO.getStartDate())
            .endDate(workerDTO.getEndDate())
            .build();
        if (workerDTO.getCoordinates() != null) {
            updated = updated.withCoordinates(
                Coordinates.builder().x(workerDTO.getCoordinates().getX()).y(workerDTO.getCoordinates().getY()).build()
            );
        }
        if (workerDTO.getOrganization() != null && workerDTO.getOrganization().getId() != null) {
            updated = updated.withOrganization(findOrganization(workerDTO.getOrganization().getId()));
        }
        if (workerDTO.getPerson() != null && workerDTO.getPerson().getId() != null) {
            updated = updated.withPerson(findPerson(workerDTO.getPerson().getId()));
        }
        return workerMapper.toDTO(workerRepository.save(updated));
    }
    
    @Transactional
    public WorkerDTO updateWithNested(Long id, CreateWorkerDTO dto) {
        var endDate = StringUtils.isNotBlank(dto.getEndDate()) ? ZonedDateTime.parse(dto.getEndDate()) : null;
        validateDates(dto.getStartDate(), endDate);
        var existing = workerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Работник не найден"));
        var updated = existing.toBuilder()
            .coordinates(Coordinates.builder().x(dto.getCoordinatesX()).y(dto.getCoordinatesY()).build())
            .salary(dto.getSalary())
            .rating(dto.getRating())
            .startDate(dto.getStartDate())
            .position(Position.valueOf(dto.getPosition()))
            .endDate(endDate)
            .organization(resolveOrganization(dto))
            .person(resolvePerson(dto))
            .build();
        return workerMapper.toDTO(workerRepository.save(updated));
    }

    @Transactional
    public void delete(Long id) {
        workerRepository.deleteById(id);
    }

    @Transactional
    public void deleteWorkerByEndDate(ZonedDateTime endDate) {
        workerRepository.findFirstByEndDate(endDate).ifPresent(workerRepository::delete);
    }

    public long countByPersonHeightGreaterThan(int height) {
        return workerRepository.countByPerson_HeightGreaterThan(height);
    }

    public List<Date> getUniqueStartDates() {
        return workerRepository.findDistinctStartDates();
    }

    @Transactional
    public WorkerDTO hireWorkerToOrganization(WorkerDTO workerDTO, Long organizationId) {
        var worker = workerMapper.toEntity(workerDTO).withOrganization(findOrganization(organizationId));
        return workerMapper.toDTO(workerRepository.save(worker));
    }

    @Transactional
    public void indexSalariesByOrganization(Long organizationId, double coefficient) {
        var workers = workerRepository.findByOrganizationId(organizationId);
        var updated = StreamEx.of(workers)
            .map(w -> w.withSalary(w.getSalary() * coefficient))
            .toList();
        workerRepository.saveAll(updated);
    }

    @Transactional
    public List<WorkerDTO> batchUpdatePerson(Map<Long, Long> workerToPersonMap) {
        var workers = workerRepository.findAllById(workerToPersonMap.keySet());
        var persons = StreamEx.of(personRepository.findAllById(workerToPersonMap.values()))
            .toMap(Person::getId, p -> p);
        var updated = StreamEx.of(workers)
            .map(w -> {
                var personId = workerToPersonMap.get(w.getId());
                return personId != null && persons.containsKey(personId) ? w.withPerson(persons.get(personId)) : w;
            })
            .toList();
        return StreamEx.of(workerRepository.saveAll(updated)).map(workerMapper::toDTO).toList();
    }

    @Transactional
    public List<WorkerDTO> batchUpdateOrganization(Map<Long, Long> workerToOrgMap) {
        var workers = workerRepository.findAllById(workerToOrgMap.keySet());
        var orgs = StreamEx.of(organizationRepository.findAllById(workerToOrgMap.values()))
            .toMap(Organization::getId, o -> o);
        var updated = StreamEx.of(workers)
            .map(w -> {
                var orgId = workerToOrgMap.get(w.getId());
                return orgId != null && orgs.containsKey(orgId) ? w.withOrganization(orgs.get(orgId)) : w;
            })
            .toList();
        return StreamEx.of(workerRepository.saveAll(updated)).map(workerMapper::toDTO).toList();
    }

    public List<Long> getRelatedWorkerIds(Long personId) {
        return workerRepository.findIdsByPersonId(personId);
    }

    public List<Long> getRelatedWorkerIdsByOrganization(Long orgId) {
        return workerRepository.findIdsByOrganizationId(orgId);
    }

    private Organization findOrganization(Long id) {
        return organizationRepository.findById(id).orElseThrow(() -> new RuntimeException("Организация не найдена"));
    }

    private Person findPerson(Long id) {
        return personRepository.findById(id).orElseThrow(() -> new RuntimeException("Персона не найдена"));
    }

    private Location findLocation(Long id) {
        return locationRepository.findById(id).orElseThrow(() -> new RuntimeException("Локация не найдена"));
    }

    private Address findAddress(Long id) {
        return addressRepository.findById(id).orElseThrow(() -> new RuntimeException("Адрес не найден"));
    }

    private void validateDates(Date startDate, ZonedDateTime endDate) {
        if (endDate == null || startDate == null) {
            return;
        }
        if (endDate.toInstant().isBefore(startDate.toInstant())) {
            throw new IllegalArgumentException("Дата окончания не может быть раньше даты начала");
        }
    }
}
