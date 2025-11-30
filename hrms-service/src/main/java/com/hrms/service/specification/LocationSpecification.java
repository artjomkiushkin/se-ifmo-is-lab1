package com.hrms.service.specification;

import com.hrms.core.model.dto.FilterDTO;
import com.hrms.core.model.entity.Location;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class LocationSpecification {

    public static Specification<Location> filterBy(List<FilterDTO> filters) {
        return BaseSpecification.filterBy(filters, LocationSpecification::convertValue);
    }

    private static Object convertValue(String field, Object value) {
        if (value == null) {
            return null;
        }
        if (field.endsWith("id") || field.endsWith("Id")) {
            return Long.parseLong(value.toString());
        }
        return value;
    }
}
