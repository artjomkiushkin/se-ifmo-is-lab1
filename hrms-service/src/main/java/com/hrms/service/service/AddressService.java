package com.hrms.service.service;

import com.hrms.core.mapper.AddressMapper;
import com.hrms.core.model.dto.AddressDTO;
import com.hrms.core.model.dto.FilterDTO;
import com.hrms.core.model.entity.Address;
import com.hrms.service.repository.AddressRepository;
import com.hrms.service.repository.OrganizationRepository;
import com.hrms.service.specification.AddressSpecification;
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
public class AddressService {
    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper;
    private final OrganizationRepository organizationRepository;

    public List<AddressDTO> findAll() {
        return StreamEx.of(addressRepository.findAll()).map(addressMapper::toDTO).toList();
    }

    public Page<AddressDTO> findAll(Pageable pageable) {
        return addressRepository.findAll(pageable).map(addressMapper::toDTO);
    }

    public Page<AddressDTO> findByFilters(List<FilterDTO> filters, Pageable pageable) {
        return addressRepository.findAll(AddressSpecification.filterBy(filters), pageable).map(addressMapper::toDTO);
    }

    public AddressDTO findById(Long id) {
        return addressRepository.findById(id)
            .map(addressMapper::toDTO)
            .orElseThrow(() -> new RuntimeException("Адрес не найден"));
    }

    @Transactional
    public AddressDTO create(AddressDTO addressDTO) {
        var address = addressMapper.toEntity(addressDTO);
        return addressMapper.toDTO(addressRepository.save(address));
    }

    @Transactional
    public AddressDTO update(Long id, AddressDTO addressDTO) {
        var existing = addressRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Адрес не найден"));
        var updated = existing.withZipCode(addressDTO.getZipCode());
        return addressMapper.toDTO(addressRepository.save(updated));
    }

    public List<Long> getRelatedOrganizationIds(Long addressId) {
        return organizationRepository.findIdsByOfficialAddressId(addressId);
    }

    @Transactional
    public List<Long> deleteWithReplacements(Long id, Map<Long, Long> orgToAddressMap) {
        var toDelete = addressRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Адрес не найден"));
        List<Long> updatedOrgIds = List.of();
        if (MapUtils.isNotEmpty(orgToAddressMap)) {
            var orgs = organizationRepository.findAllById(orgToAddressMap.keySet());
            var addressIds = StreamEx.of(orgToAddressMap.values()).filter(aid -> aid != null).toList();
            var addresses = StreamEx.of(addressRepository.findAllById(addressIds)).toMap(Address::getId, a -> a);
            var updated = StreamEx.of(orgs)
                .map(o -> {
                    var newAddrId = orgToAddressMap.get(o.getId());
                    if (newAddrId == null) {
                        return o.withOfficialAddress(null);
                    }
                    return addresses.containsKey(newAddrId) ? o.withOfficialAddress(addresses.get(newAddrId)) : o;
                })
                .toList();
            organizationRepository.saveAll(updated);
            updatedOrgIds = StreamEx.of(updated).map(o -> o.getId()).toList();
        }
        addressRepository.delete(toDelete);
        return updatedOrgIds;
    }

    @Transactional
    public void delete(Long id, Long replacementId) {
        var toDelete = addressRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Адрес не найден"));
        var relatedOrgIds = organizationRepository.findIdsByOfficialAddressId(id);
        if (!relatedOrgIds.isEmpty()) {
            if (replacementId == null) {
                throw new RuntimeException("Адрес используется в " + relatedOrgIds.size() + " организациях. Укажите замену.");
            }
            var replacement = addressRepository.findById(replacementId)
                .orElseThrow(() -> new RuntimeException("Адрес для замены не найден"));
            var orgs = organizationRepository.findAllById(relatedOrgIds);
            var updated = StreamEx.of(orgs).map(o -> o.withOfficialAddress(replacement)).toList();
            organizationRepository.saveAll(updated);
        }
        addressRepository.delete(toDelete);
    }
}
