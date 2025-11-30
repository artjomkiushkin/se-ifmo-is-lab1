package com.hrms.service.specification;

import com.hrms.core.model.dto.FilterDTO;
import com.hrms.core.model.entity.Address;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class AddressSpecification {

    public static Specification<Address> filterBy(List<FilterDTO> filters) {
        return BaseSpecification.filterBy(filters, AddressSpecification::convertValue);
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
