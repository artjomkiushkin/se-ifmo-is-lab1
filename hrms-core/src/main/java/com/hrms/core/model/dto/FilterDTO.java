package com.hrms.core.model.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilterDTO {
    private String field;
    private String operator;
    private Object value;
}

