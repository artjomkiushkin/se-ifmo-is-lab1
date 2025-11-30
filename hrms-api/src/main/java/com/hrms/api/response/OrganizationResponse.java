package com.hrms.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationResponse {
    private Long id;
    private AddressResponse officialAddress;
    private Float annualTurnover;
    private Long employeesCount;
    private String fullName;
    private Float rating;
    private String type;
}

