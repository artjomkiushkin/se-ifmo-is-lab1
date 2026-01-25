package com.hrms.core.mapper;

import com.hrms.core.model.dto.ImportOperationDTO;
import com.hrms.core.model.entity.ImportOperation;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ImportOperationMapper {
    ImportOperationDTO toDTO(ImportOperation entity);
    ImportOperation toEntity(ImportOperationDTO dto);
    List<ImportOperationDTO> toDTOList(List<ImportOperation> entities);
}

