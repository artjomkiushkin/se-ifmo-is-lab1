package com.hrms.api.mapper;

import com.hrms.api.response.ImportOperationResponse;
import com.hrms.core.model.dto.ImportOperationDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ImportOperationFacadeMapper {
    ImportOperationResponse toResponse(ImportOperationDTO dto);
}

