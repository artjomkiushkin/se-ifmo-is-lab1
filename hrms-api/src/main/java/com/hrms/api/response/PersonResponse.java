package com.hrms.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonResponse {
    private Long id;
    private String name;
    private String eyeColor;
    private String hairColor;
    private LocationResponse location;
    private Long height;
    private String nationality;
}

