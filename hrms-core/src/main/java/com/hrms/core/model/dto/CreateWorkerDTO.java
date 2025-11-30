package com.hrms.core.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateWorkerDTO {
    private Double coordinatesX;
    private Float coordinatesY;
    private Long organizationId;
    private OrganizationDTO newOrganization;
    private OrganizationDTO editOrganization;
    private Double salary;
    private Float rating;
    private Date startDate;
    private String endDate;
    private String position;
    private Long personId;
    private PersonDTO newPerson;
    private PersonDTO editPerson;
}

