package com.hrms.api.mapper;

import com.hrms.api.request.BatchUpdateOrganizationRequest;
import com.hrms.api.request.CreateOrganizationRequest;
import com.hrms.api.request.UpdateOrganizationRequest;
import com.hrms.api.response.OrganizationResponse;
import com.hrms.core.model.dto.OrganizationDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

@Mapper(componentModel = "spring", implementationName = "OrganizationFacadeMapperImpl")
public interface OrganizationFacadeMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "officialAddress.id", source = "officialAddressId")
    @Mapping(target = "officialAddress.zipCode", source = "zipCode")
    @Mapping(target = "annualTurnover", source = "annualTurnover", qualifiedByName = "toFloat")
    @Mapping(target = "rating", source = "rating", qualifiedByName = "toFloat")
    @Mapping(target = "employeesCount", source = "employeesCount", qualifiedByName = "toLong")
    OrganizationDTO toDTO(CreateOrganizationRequest request);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "officialAddress.id", source = "officialAddressId")
    @Mapping(target = "officialAddress.zipCode", source = "zipCode")
    @Mapping(target = "annualTurnover", source = "annualTurnover", qualifiedByName = "toFloat")
    @Mapping(target = "rating", source = "rating", qualifiedByName = "toFloat")
    @Mapping(target = "employeesCount", source = "employeesCount", qualifiedByName = "toLong")
    OrganizationDTO toDTO(UpdateOrganizationRequest request);
    
    @Mapping(target = "officialAddress.id", source = "officialAddressId")
    OrganizationDTO toDTO(BatchUpdateOrganizationRequest request);
    
    OrganizationResponse toResponse(OrganizationDTO dto);
    
    List<OrganizationResponse> toResponseList(List<OrganizationDTO> dtos);
    
    @Named("toFloat")
    default Float toFloat(BigDecimal value) {
        return value != null ? value.floatValue() : null;
    }
    
    @Named("toLong")
    default Long toLong(BigInteger value) {
        return value != null ? value.longValue() : null;
    }
}
