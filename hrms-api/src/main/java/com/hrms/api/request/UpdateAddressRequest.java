package com.hrms.api.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateAddressRequest {
    @NotBlank(message = "Почтовый индекс не может быть пустым")
    private String zipCode;
}

