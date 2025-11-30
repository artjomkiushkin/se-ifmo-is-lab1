package com.hrms.service.service;

import com.hrms.core.mapper.AddressMapper;
import com.hrms.core.mapper.OrganizationMapper;
import com.hrms.core.model.dto.FilterDTO;
import com.hrms.core.model.dto.OrganizationDTO;
import com.hrms.core.model.entity.Address;
import com.hrms.core.model.entity.Organization;
import com.hrms.service.repository.AddressRepository;
import com.hrms.service.repository.OrganizationRepository;
import com.hrms.service.repository.WorkerRepository;
import com.hrms.service.specification.OrganizationSpecification;
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
public class OrganizationService {
    
    private final OrganizationRepository organizationRepository;
    private final OrganizationMapper organizationMapper;
    private final AddressRepository addressRepository;
    private final WorkerRepository workerRepository;
    private final AddressMapper addressMapper;
    private final NotificationService notificationService;
    
    // private static final double DEFAULT_COEFFICIENT = 1.1;

    public List<OrganizationDTO> findAll() {
        return StreamEx.of(organizationRepository.findAll()).map(organizationMapper::toDTO).toList();
    }

    public Page<OrganizationDTO> findAll(Pageable pageable) {
        return organizationRepository.findAll(pageable).map(organizationMapper::toDTO);
    }

    public Page<OrganizationDTO> findByFilters(List<FilterDTO> filters, Pageable pageable) {
        return organizationRepository.findAll(OrganizationSpecification.filterBy(filters), pageable).map(organizationMapper::toDTO);
    }

    public OrganizationDTO findById(Long id) {
        return organizationRepository.findById(id)
            .map(organizationMapper::toDTO)
            .orElseThrow(() -> new RuntimeException("Организация не найдена"));
    }
    
    public List<OrganizationDTO> findByIds(List<Long> ids) {
        return StreamEx.of(organizationRepository.findAllById(ids)).map(organizationMapper::toDTO).toList();
    }

    @Transactional
    public OrganizationDTO create(OrganizationDTO organizationDTO) {
        var organization = organizationMapper.toEntity(organizationDTO);
        organization.setOfficialAddress(resolveAddress(organizationDTO));
        return organizationMapper.toDTO(organizationRepository.save(organization));
    }

    @Transactional
    public OrganizationDTO update(Long id, OrganizationDTO organizationDTO) {
        organizationRepository.findById(id).orElseThrow(() -> new RuntimeException("Организация не найдена"));
        var organization = organizationMapper.toEntity(organizationDTO.withId(id));
        organization.setOfficialAddress(resolveAddress(organizationDTO));
        return organizationMapper.toDTO(organizationRepository.save(organization));
    }
    
    private Address resolveAddress(OrganizationDTO dto) {
        if (dto.getOfficialAddress() == null) {
            return null;
        }
        var addrDto = dto.getOfficialAddress();
        if (addrDto.getId() != null && addrDto.getZipCode() != null) {
            var existing = addressRepository.findById(addrDto.getId())
                .orElseThrow(() -> new RuntimeException("Адрес не найден"));
            var updated = existing.withZipCode(addrDto.getZipCode());
            var saved = addressRepository.save(updated);
            notificationService.notifyAddressUpdated(addressMapper.toDTO(saved));
            return saved;
        }
        if (addrDto.getId() != null) {
            return addressRepository.findById(addrDto.getId())
                .orElseThrow(() -> new RuntimeException("Адрес не найден"));
        }
        if (addrDto.getZipCode() != null) {
            var created = addressRepository.save(Address.builder().zipCode(addrDto.getZipCode()).build());
            notificationService.notifyAddressCreated(addressMapper.toDTO(created));
            return created;
        }
        return null;
    }

    public List<Long> getRelatedWorkerIds(Long orgId) {
        return workerRepository.findIdsByOrganizationId(orgId);
    }

    @Transactional
    public List<Long> deleteWithReplacements(Long id, Map<Long, Long> workerToOrgMap) {
        var toDelete = organizationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Организация не найдена"));
        List<Long> updatedWorkerIds = List.of();
        if (MapUtils.isNotEmpty(workerToOrgMap)) {
            var workers = workerRepository.findAllById(workerToOrgMap.keySet());
            var orgs = StreamEx.of(organizationRepository.findAllById(workerToOrgMap.values())).toMap(Organization::getId, o -> o);
            var updated = StreamEx.of(workers)
                .map(w -> {
                    var newOrgId = workerToOrgMap.get(w.getId());
                    return newOrgId != null && orgs.containsKey(newOrgId) ? w.withOrganization(orgs.get(newOrgId)) : w;
                })
                .toList();
            workerRepository.saveAll(updated);
            updatedWorkerIds = StreamEx.of(updated).map(w -> w.getId()).toList();
        }
        organizationRepository.delete(toDelete);
        return updatedWorkerIds;
    }

    @Transactional
    public void delete(Long id) {
        organizationRepository.deleteById(id);
    }

    @Transactional
    public void indexSalaries(Long organizationId, double coefficient) {
        if (coefficient <= 1) {
            throw new IllegalArgumentException("Коэффициент должен быть больше 1");
        }
        var workers = workerRepository.findByOrganizationId(organizationId);
        var updated = StreamEx.of(workers)
            .map(w -> w.withSalary(w.getSalary() * coefficient))
            .toList();
        workerRepository.saveAll(updated);
    }

    @Transactional
    public List<OrganizationDTO> batchUpdateAddress(List<Long> orgIds, List<Long> addressIds) {
        var orgs = organizationRepository.findAllById(orgIds);
        var addresses = StreamEx.of(addressRepository.findAllById(addressIds))
            .toMap(a -> a.getId(), a -> a);
        var updated = StreamEx.of(orgs)
            .mapToEntry(o -> orgIds.indexOf(o.getId()))
            .mapValues(idx -> idx >= 0 && idx < addressIds.size() ? addressIds.get(idx) : null)
            .mapKeyValue((org, addrId) -> addrId != null && addresses.containsKey(addrId) 
                ? org.withOfficialAddress(addresses.get(addrId)) 
                : org.withOfficialAddress(null))
            .toList();
        return StreamEx.of(organizationRepository.saveAll(updated)).map(organizationMapper::toDTO).toList();
    }
}
