package com.hrms.api.mapper;

import com.hrms.api.request.CreateWorkerRequest;
import com.hrms.api.request.UpdateWorkerRequest;
import com.hrms.api.request.nested.CreateAddressNested;
import com.hrms.api.request.nested.CreateLocationNested;
import com.hrms.api.request.nested.CreateOrganizationNested;
import com.hrms.api.request.nested.CreatePersonNested;
import com.hrms.api.response.WorkerResponse;
import com.hrms.core.model.dto.*;
import com.hrms.core.model.enums.Color;
import com.hrms.core.model.enums.Country;
import com.hrms.core.model.enums.OrganizationType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.math.BigInteger;

@Mapper(componentModel = "spring", implementationName = "WorkerFacadeMapperImpl")
public interface WorkerFacadeMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "coordinates.x", source = "coordinatesX", qualifiedByName = "toDouble")
    @Mapping(target = "coordinates.y", source = "coordinatesY", qualifiedByName = "toFloat")
    @Mapping(target = "salary", source = "salary", qualifiedByName = "toDouble")
    @Mapping(target = "rating", source = "rating", qualifiedByName = "toFloat")
    @Mapping(target = "organization.id", source = "organizationId")
    @Mapping(target = "person.id", source = "personId")
    WorkerDTO toDTO(CreateWorkerRequest request);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "coordinates.x", source = "coordinatesX", qualifiedByName = "toDouble")
    @Mapping(target = "coordinates.y", source = "coordinatesY", qualifiedByName = "toFloat")
    @Mapping(target = "salary", source = "salary", qualifiedByName = "toDouble")
    @Mapping(target = "rating", source = "rating", qualifiedByName = "toFloat")
    @Mapping(target = "organization.id", source = "organizationId")
    @Mapping(target = "person.id", source = "personId")
    WorkerDTO toDTO(UpdateWorkerRequest request);
    
    @Mapping(target = "coordinatesX", source = "coordinatesX", qualifiedByName = "toDouble")
    @Mapping(target = "coordinatesY", source = "coordinatesY", qualifiedByName = "toFloat")
    @Mapping(target = "salary", source = "salary", qualifiedByName = "toDouble")
    @Mapping(target = "rating", source = "rating", qualifiedByName = "toFloat")
    @Mapping(target = "newOrganization", source = "newOrganization", qualifiedByName = "toOrganizationDTO")
    @Mapping(target = "editOrganization", source = "editOrganization", qualifiedByName = "toEditOrganizationDTO")
    @Mapping(target = "newPerson", source = "newPerson", qualifiedByName = "toPersonDTO")
    @Mapping(target = "editPerson", source = "editPerson", qualifiedByName = "toEditPersonDTO")
    CreateWorkerDTO toCreateDTO(CreateWorkerRequest request);
    
    @Mapping(target = "coordinatesX", source = "coordinatesX", qualifiedByName = "toDouble")
    @Mapping(target = "coordinatesY", source = "coordinatesY", qualifiedByName = "toFloat")
    @Mapping(target = "salary", source = "salary", qualifiedByName = "toDouble")
    @Mapping(target = "rating", source = "rating", qualifiedByName = "toFloat")
    @Mapping(target = "newOrganization", ignore = true)
    @Mapping(target = "editOrganization", source = "editOrganization", qualifiedByName = "toEditOrganizationDTO")
    @Mapping(target = "newPerson", ignore = true)
    @Mapping(target = "editPerson", source = "editPerson", qualifiedByName = "toEditPersonDTO")
    CreateWorkerDTO toUpdateDTO(UpdateWorkerRequest request);
    
    WorkerResponse toResponse(WorkerDTO dto);
    
    @Named("toDouble")
    default Double toDouble(BigDecimal value) {
        return value != null ? value.doubleValue() : null;
    }
    
    @Named("toFloat")
    default Float toFloat(BigDecimal value) {
        return value != null ? value.floatValue() : null;
    }
    
    @Named("toLong")
    default Long toLong(BigInteger value) {
        return value != null ? value.longValue() : null;
    }
    
    @Named("toOrganizationDTO")
    default OrganizationDTO toOrganizationDTO(CreateOrganizationNested nested) {
        if (nested == null) {
            return null;
        }
        return OrganizationDTO.builder()
            .fullName(nested.getFullName())
            .annualTurnover(nested.getAnnualTurnover() != null ? nested.getAnnualTurnover().floatValue() : null)
            .rating(nested.getRating() != null ? nested.getRating().floatValue() : null)
            .employeesCount(nested.getEmployeesCount() != null ? nested.getEmployeesCount().longValue() : null)
            .type(nested.getType() != null ? OrganizationType.valueOf(nested.getType()) : null)
            .officialAddress(toAddressDTO(nested.getOfficialAddressId(), nested.getNewAddress()))
            .build();
    }
    
    @Named("toEditOrganizationDTO")
    default OrganizationDTO toEditOrganizationDTO(CreateOrganizationNested nested) {
        if (nested == null) {
            return null;
        }
        return OrganizationDTO.builder()
            .id(nested.getId())
            .fullName(nested.getFullName())
            .annualTurnover(nested.getAnnualTurnover() != null ? nested.getAnnualTurnover().floatValue() : null)
            .rating(nested.getRating() != null ? nested.getRating().floatValue() : null)
            .employeesCount(nested.getEmployeesCount() != null ? nested.getEmployeesCount().longValue() : null)
            .type(nested.getType() != null ? OrganizationType.valueOf(nested.getType()) : null)
            .officialAddress(toEditAddressDTO(nested.getOfficialAddressId(), nested.getZipCode(), nested.getNewAddress()))
            .build();
    }
    
    default AddressDTO toEditAddressDTO(Long addressId, String zipCode, CreateAddressNested newAddress) {
        if (addressId != null && zipCode != null) {
            return AddressDTO.builder().id(addressId).zipCode(zipCode).build();
        }
        if (addressId != null) {
            return AddressDTO.builder().id(addressId).build();
        }
        if (newAddress != null) {
            return AddressDTO.builder().zipCode(newAddress.getZipCode()).build();
        }
        if (zipCode != null) {
            return AddressDTO.builder().zipCode(zipCode).build();
        }
        return null;
    }
    
    default AddressDTO toAddressDTO(Long addressId, CreateAddressNested newAddress) {
        if (addressId != null) {
            return AddressDTO.builder().id(addressId).build();
        }
        if (newAddress != null) {
            return AddressDTO.builder().zipCode(newAddress.getZipCode()).build();
        }
        return null;
    }
    
    @Named("toPersonDTO")
    default PersonDTO toPersonDTO(CreatePersonNested nested) {
        if (nested == null) {
            return null;
        }
        return PersonDTO.builder()
            .name(nested.getName())
            .eyeColor(nested.getEyeColor() != null ? Color.valueOf(nested.getEyeColor()) : null)
            .hairColor(nested.getHairColor() != null ? Color.valueOf(nested.getHairColor()) : null)
            .height(nested.getHeight() != null ? nested.getHeight().longValue() : null)
            .nationality(nested.getNationality() != null ? Country.valueOf(nested.getNationality()) : null)
            .location(toLocationDTO(nested.getLocationId(), nested.getNewLocation()))
            .build();
    }
    
    @Named("toEditPersonDTO")
    default PersonDTO toEditPersonDTO(CreatePersonNested nested) {
        if (nested == null) {
            return null;
        }
        return PersonDTO.builder()
            .id(nested.getId())
            .name(nested.getName())
            .eyeColor(nested.getEyeColor() != null ? Color.valueOf(nested.getEyeColor()) : null)
            .hairColor(nested.getHairColor() != null ? Color.valueOf(nested.getHairColor()) : null)
            .height(nested.getHeight() != null ? nested.getHeight().longValue() : null)
            .nationality(nested.getNationality() != null ? Country.valueOf(nested.getNationality()) : null)
            .location(toEditLocationDTO(nested))
            .build();
    }
    
    default LocationDTO toEditLocationDTO(CreatePersonNested nested) {
        if (nested.getLocationId() != null && nested.getLocationZ() != null) {
            return LocationDTO.builder()
                .id(nested.getLocationId())
                .x(nested.getLocationX() != null ? nested.getLocationX().longValue() : 0)
                .y(nested.getLocationY() != null ? nested.getLocationY().doubleValue() : 0)
                .z(nested.getLocationZ().longValue())
                .name(nested.getLocationName())
                .build();
        }
        if (nested.getLocationId() != null) {
            return LocationDTO.builder().id(nested.getLocationId()).build();
        }
        if (nested.getNewLocation() != null) {
            return LocationDTO.builder()
                .x(nested.getNewLocation().getX())
                .y(nested.getNewLocation().getY())
                .z(nested.getNewLocation().getZ())
                .name(nested.getNewLocation().getName())
                .build();
        }
        if (nested.getLocationZ() != null) {
            return LocationDTO.builder()
                .x(nested.getLocationX() != null ? nested.getLocationX().longValue() : 0)
                .y(nested.getLocationY() != null ? nested.getLocationY().doubleValue() : 0)
                .z(nested.getLocationZ().longValue())
                .name(nested.getLocationName())
                .build();
        }
        return null;
    }
    
    default LocationDTO toLocationDTO(Long locationId, CreateLocationNested newLocation) {
        if (locationId != null) {
            return LocationDTO.builder().id(locationId).build();
        }
        if (newLocation != null) {
            return LocationDTO.builder()
                .x(newLocation.getX())
                .y(newLocation.getY())
                .z(newLocation.getZ())
                .name(newLocation.getName())
                .build();
        }
        return null;
    }
}
