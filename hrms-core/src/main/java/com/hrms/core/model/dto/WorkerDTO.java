package com.hrms.core.model.dto;

import com.hrms.core.model.enums.Position;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.Date;

@Data
@Builder(toBuilder = true)
@With
@NoArgsConstructor
@AllArgsConstructor
public class WorkerDTO {
    private Long id;
    private CoordinatesData coordinates;
    private ZonedDateTime creationDate;
    private OrganizationDTO organization;
    private double salary;
    private float rating;
    private Date startDate;
    private ZonedDateTime endDate;
    private Position position;
    private PersonDTO person;
    
    @Data
    @Builder(toBuilder = true)
    @With
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CoordinatesData {
        private Double x;
        private Float y;
    }
}

