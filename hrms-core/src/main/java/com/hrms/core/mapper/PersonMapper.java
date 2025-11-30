package com.hrms.core.mapper;

import com.hrms.core.model.dto.PersonDTO;
import com.hrms.core.model.entity.Person;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = {LocationMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PersonMapper {
    PersonDTO toDTO(Person person);
    Person toEntity(PersonDTO personDTO);
}

