package com.hrms.api.request.nested;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLocationNested {
    @NotNull(message = "X не может быть null")
    private Long x;
    
    @NotNull(message = "Y не может быть null")
    private Double y;
    
    @NotNull(message = "Z не может быть null")
    private Long z;
    
    private String name;
}

