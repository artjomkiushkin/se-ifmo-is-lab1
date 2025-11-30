package com.hrms.api.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateLocationRequest {
    @NotNull(message = "X координата не может быть null")
    private Long x;
    
    @NotNull(message = "Y координата не может быть null")
    private Double y;
    
    @NotNull(message = "Z координата не может быть null")
    private Long z;
    
    @Size(min = 1, message = "Название не может быть пустой строкой")
    private String name;
}

