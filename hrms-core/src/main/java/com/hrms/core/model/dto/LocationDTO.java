package com.hrms.core.model.dto;

import lombok.*;

@Data
@Builder(toBuilder = true)
@With
@NoArgsConstructor
@AllArgsConstructor
public class LocationDTO {
    private Long id;
    private long x;
    private double y;
    private Long z;
    private String name;
}

