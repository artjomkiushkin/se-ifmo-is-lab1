package com.hrms.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EnumValueResponse {
    private String name;
    private String displayName;
}

