package com.hrms.core.mapper;

import com.hrms.core.model.dto.WorkerDTO;
import com.hrms.core.model.entity.Worker;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = {OrganizationMapper.class, PersonMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WorkerMapper {
    WorkerDTO toDTO(Worker worker);
    Worker toEntity(WorkerDTO workerDTO);
}

