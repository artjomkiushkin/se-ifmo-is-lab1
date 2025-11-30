package com.hrms.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerResponse {
    private Long id;
    private CoordinatesData coordinates;
    private ZonedDateTime creationDate;
    private OrganizationResponse organization;
    private Double salary;
    private Float rating;
    private Date startDate;
    private ZonedDateTime endDate;
    private String position;
    private PersonResponse person;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CoordinatesData {
        private Double x;
        private Float y;
    }
}

