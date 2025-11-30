package com.hrms.service.specification;

import com.hrms.core.model.dto.FilterDTO;
import com.hrms.core.model.entity.Organization;
import com.hrms.core.model.entity.Person;
import com.hrms.core.model.entity.Worker;
import com.hrms.core.model.enums.Color;
import com.hrms.core.model.enums.Country;
import com.hrms.core.model.enums.OrganizationType;
import com.hrms.core.model.enums.Position;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class WorkerSpecification {
    
    // TODO: вынести маппинг полей в отдельный enum?
    
    public static Specification<Worker> filterBy(List<FilterDTO> filters) {
        return BaseSpecification.filterBy(filters, WorkerSpecification::convertValue);
    }

    private static Object convertValue(String field, Object value) {
        if (value == null) {
            return null;
        }
        var str = value.toString();
        return switch (field) {
            case Worker.Fields.position -> Position.valueOf(str);
            case Person.Fields.eyeColor, Person.Fields.hairColor,
                 "person." + Person.Fields.eyeColor, "person." + Person.Fields.hairColor -> Color.valueOf(str);
            case Person.Fields.nationality, "person." + Person.Fields.nationality -> Country.valueOf(str);
            case Organization.Fields.type, "organization." + Organization.Fields.type -> OrganizationType.valueOf(str);
            default -> {
                if (field.endsWith("id") || field.endsWith("Id")) {
                    yield Long.parseLong(str);
                }
                yield value;
            }
        };
    }
}
