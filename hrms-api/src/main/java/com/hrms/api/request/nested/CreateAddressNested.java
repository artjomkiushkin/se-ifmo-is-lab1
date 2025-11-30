package com.hrms.api.request.nested;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAddressNested {
    @NotBlank(message = "Почтовый индекс не может быть пустым")
    private String zipCode;
}

