package com.hrms.service.specification;

import com.hrms.core.model.dto.FilterDTO;
import com.hrms.core.model.entity.Organization;
import com.hrms.core.model.enums.OrganizationType;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class OrganizationSpecification {

    public static Specification<Organization> filterBy(List<FilterDTO> filters) {
        return BaseSpecification.filterBy(filters, OrganizationSpecification::convertValue);
    }

    private static Object convertValue(String field, Object value) {
        if (value == null) {
            return null;
        }
        var str = value.toString();
        return switch (field) {
            case Organization.Fields.type -> OrganizationType.valueOf(str);
            default -> {
                if (field.endsWith("id") || field.endsWith("Id")) {
                    yield Long.parseLong(str);
                }
                yield value;
            }
        };
    }
}
