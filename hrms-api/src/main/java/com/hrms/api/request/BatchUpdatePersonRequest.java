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
public class BatchUpdatePersonRequest {
    @NotNull(message = "ID не может быть null")
    private Long id;
    
    private Long locationX;
    private Double locationY;
    private Long locationZ;
    private String locationName;
}
