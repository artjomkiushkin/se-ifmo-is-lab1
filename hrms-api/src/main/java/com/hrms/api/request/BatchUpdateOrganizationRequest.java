package com.hrms.api.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchUpdateOrganizationRequest {
    @NotNull(message = "ID организации не может быть null")
    private Long id;
    
    private Long officialAddressId;
}
