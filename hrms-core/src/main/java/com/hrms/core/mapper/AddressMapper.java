package com.hrms.core.mapper;

import com.hrms.core.model.dto.AddressDTO;
import com.hrms.core.model.entity.Address;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AddressMapper {
    AddressDTO toDTO(Address address);
    Address toEntity(AddressDTO addressDTO);
}

