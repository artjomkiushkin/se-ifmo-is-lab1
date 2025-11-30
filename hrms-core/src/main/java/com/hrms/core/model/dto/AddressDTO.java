package com.hrms.core.model.dto;

import lombok.*;

@Data
@Builder(toBuilder = true)
@With
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {
    private Long id;
    private String zipCode;
}

