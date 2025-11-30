package com.hrms.api.mapper;

import com.hrms.api.request.BatchUpdatePersonRequest;
import com.hrms.api.request.CreatePersonRequest;
import com.hrms.api.request.UpdatePersonRequest;
import com.hrms.api.response.PersonResponse;
import com.hrms.core.model.dto.LocationDTO;
import com.hrms.core.model.dto.PersonDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

@Mapper(componentModel = "spring", implementationName = "PersonFacadeMapperImpl")
public interface PersonFacadeMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "location", source = "request", qualifiedByName = "mapLocationFromCreate")
    @Mapping(target = "height", source = "height", qualifiedByName = "toLong")
    PersonDTO toDTO(CreatePersonRequest request);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "location", source = "request", qualifiedByName = "mapLocationFromUpdate")
    @Mapping(target = "height", source = "height", qualifiedByName = "toLong")
    PersonDTO toDTO(UpdatePersonRequest request);
    
    @Mapping(target = "location", source = "request", qualifiedByName = "mapLocationFromBatch")
    PersonDTO toDTO(BatchUpdatePersonRequest request);
    
    PersonResponse toResponse(PersonDTO dto);
    
    List<PersonResponse> toResponseList(List<PersonDTO> dtos);
    
    @Named("toLong")
    default Long toLong(BigInteger value) {
        return value != null ? value.longValue() : null;
    }
    
    @Named("mapLocationFromCreate")
    default LocationDTO mapLocationFromCreate(CreatePersonRequest request) {
        if (request.getLocationId() != null) {
            return LocationDTO.builder()
                .id(request.getLocationId())
                .x(request.getLocationX() != null ? request.getLocationX().longValue() : null)
                .y(request.getLocationY() != null ? request.getLocationY().doubleValue() : null)
                .z(request.getLocationZ() != null ? request.getLocationZ().longValue() : null)
                .name(request.getLocationName())
                .build();
        }
        if (request.getLocationZ() == null) {
            return null;
        }
        return LocationDTO.builder()
            .x(request.getLocationX() != null ? request.getLocationX().longValue() : null)
            .y(request.getLocationY() != null ? request.getLocationY().doubleValue() : null)
            .z(request.getLocationZ().longValue())
            .name(request.getLocationName())
            .build();
    }
    
    @Named("mapLocationFromUpdate")
    default LocationDTO mapLocationFromUpdate(UpdatePersonRequest request) {
        if (request.getLocationId() != null) {
            return LocationDTO.builder()
                .id(request.getLocationId())
                .x(request.getLocationX() != null ? request.getLocationX().longValue() : null)
                .y(request.getLocationY() != null ? request.getLocationY().doubleValue() : null)
                .z(request.getLocationZ() != null ? request.getLocationZ().longValue() : null)
                .name(request.getLocationName())
                .build();
        }
        if (request.getLocationZ() == null) {
            return null;
        }
        return LocationDTO.builder()
            .x(request.getLocationX() != null ? request.getLocationX().longValue() : null)
            .y(request.getLocationY() != null ? request.getLocationY().doubleValue() : null)
            .z(request.getLocationZ().longValue())
            .name(request.getLocationName())
            .build();
    }
    
    @Named("mapLocationFromBatch")
    default LocationDTO mapLocationFromBatch(BatchUpdatePersonRequest request) {
        if (request.getLocationZ() == null) {
            return null;
        }
        return LocationDTO.builder()
            .x(request.getLocationX())
            .y(request.getLocationY())
            .z(request.getLocationZ())
            .name(request.getLocationName())
            .build();
    }
}
