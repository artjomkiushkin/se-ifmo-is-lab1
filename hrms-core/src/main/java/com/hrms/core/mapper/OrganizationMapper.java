package com.hrms.core.mapper;

import com.hrms.core.model.dto.OrganizationDTO;
import com.hrms.core.model.entity.Organization;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = {AddressMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrganizationMapper {
    OrganizationDTO toDTO(Organization organization);
    Organization toEntity(OrganizationDTO organizationDTO);
}

