package com.hrms.api.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrganizationRequest {
    private String zipCode;
    private Long officialAddressId;
    
    @NotNull(message = "Годовой оборот обязателен")
    @DecimalMin(value = "0", inclusive = false, message = "Годовой оборот должен быть больше 0")
    @DecimalMax(value = "3.4028235E38", message = "Годовой оборот превышает допустимый диапазон Float")
    private BigDecimal annualTurnover;
    
    @DecimalMin(value = "1", message = "Количество сотрудников должно быть больше 0")
    @DecimalMax(value = "9223372036854775807", message = "Количество сотрудников превышает допустимый диапазон Long")
    private BigInteger employeesCount;
    
    @NotBlank(message = "Полное название обязательно")
    private String fullName;
    
    @NotNull(message = "Рейтинг обязателен")
    @DecimalMin(value = "0", inclusive = false, message = "Рейтинг должен быть больше 0")
    @DecimalMax(value = "3.4028235E38", message = "Рейтинг превышает допустимый диапазон Float")
    private BigDecimal rating;
    
    private String type;
}
