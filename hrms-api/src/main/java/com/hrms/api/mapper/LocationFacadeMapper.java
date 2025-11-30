package com.hrms.api.mapper;

import com.hrms.api.request.CreateLocationRequest;
import com.hrms.api.request.UpdateLocationRequest;
import com.hrms.api.response.LocationResponse;
import com.hrms.core.model.dto.LocationDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", implementationName = "LocationFacadeMapperImpl")
public interface LocationFacadeMapper {
    
    @Mapping(target = "id", ignore = true)
    LocationDTO toDTO(CreateLocationRequest request);
    
    @Mapping(target = "id", ignore = true)
    LocationDTO toDTO(UpdateLocationRequest request);
    
    LocationResponse toResponse(LocationDTO dto);
    
    List<LocationResponse> toResponseList(List<LocationDTO> dtos);
}

