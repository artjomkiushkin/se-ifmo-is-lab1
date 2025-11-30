package com.hrms.core.model.dto;

import com.hrms.core.model.enums.Color;
import com.hrms.core.model.enums.Country;
import lombok.*;

@Data
@Builder(toBuilder = true)
@With
@NoArgsConstructor
@AllArgsConstructor
public class PersonDTO {
    private Long id;
    private String name;
    private Color eyeColor;
    private Color hairColor;
    private LocationDTO location;
    private long height;
    private Country nationality;
}

