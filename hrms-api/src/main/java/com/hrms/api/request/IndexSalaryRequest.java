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
public class IndexSalaryRequest {
    @NotNull(message = "Коэффициент не может быть null")
    private Double coefficient;
}

