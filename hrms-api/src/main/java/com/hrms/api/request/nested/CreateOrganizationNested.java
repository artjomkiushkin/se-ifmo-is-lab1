package com.hrms.api.request.nested;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreateOrganizationNested {
    private Long id;
    
    private String zipCode;
    
    @NotBlank(message = "Название организации обязательно")
    private String fullName;
    
    @NotNull(message = "Годовой оборот обязателен")
    @DecimalMin(value = "0", inclusive = false, message = "Годовой оборот должен быть больше 0")
    @DecimalMax(value = "3.4028235E38", message = "Годовой оборот превышает допустимый диапазон Float")
    private BigDecimal annualTurnover;
    
    @NotNull(message = "Рейтинг обязателен")
    @DecimalMin(value = "0", inclusive = false, message = "Рейтинг должен быть больше 0")
    @DecimalMax(value = "3.4028235E38", message = "Рейтинг превышает допустимый диапазон Float")
    private BigDecimal rating;
    
    @DecimalMin(value = "1", message = "Количество сотрудников должно быть больше 0")
    @DecimalMax(value = "9223372036854775807", message = "Количество сотрудников превышает допустимый диапазон Long")
    private BigInteger employeesCount;
    
    private String type;
    
    private Long officialAddressId;
    
    @Valid
    private CreateAddressNested newAddress;
}

