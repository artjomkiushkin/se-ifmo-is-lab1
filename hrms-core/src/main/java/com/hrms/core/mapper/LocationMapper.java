package com.hrms.core.mapper;

import com.hrms.core.model.dto.LocationDTO;
import com.hrms.core.model.entity.Location;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LocationMapper {
    LocationDTO toDTO(Location location);
    Location toEntity(LocationDTO locationDTO);
}

