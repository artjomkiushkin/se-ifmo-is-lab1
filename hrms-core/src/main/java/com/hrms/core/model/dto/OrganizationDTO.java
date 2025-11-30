package com.hrms.core.model.dto;

import com.hrms.core.model.enums.OrganizationType;
import lombok.*;

@Data
@Builder(toBuilder = true)
@With
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationDTO {
    private Long id;
    private AddressDTO officialAddress;
    private float annualTurnover;
    private Long employeesCount;
    private String fullName;
    private Float rating;
    private OrganizationType type;
}

