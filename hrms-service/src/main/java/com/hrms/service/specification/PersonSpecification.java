package com.hrms.service.specification;

import com.hrms.core.model.dto.FilterDTO;
import com.hrms.core.model.entity.Person;
import com.hrms.core.model.enums.Color;
import com.hrms.core.model.enums.Country;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class PersonSpecification {

    public static Specification<Person> filterBy(List<FilterDTO> filters) {
        return BaseSpecification.filterBy(filters, PersonSpecification::convertValue);
    }

    private static Object convertValue(String field, Object value) {
        if (value == null) {
            return null;
        }
        var str = value.toString();
        return switch (field) {
            case Person.Fields.eyeColor, Person.Fields.hairColor -> Color.valueOf(str);
            case Person.Fields.nationality -> Country.valueOf(str);
            default -> {
                if (field.endsWith("id") || field.endsWith("Id")) {
                    yield Long.parseLong(str);
                }
                yield value;
            }
        };
    }
}
