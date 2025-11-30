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
public class CreatePersonNested {
    private Long id;
    
    @NotBlank(message = "Имя персоны обязательно")
    private String name;
    
    @NotNull(message = "Цвет глаз обязателен")
    private String eyeColor;
    
    @NotNull(message = "Цвет волос обязателен")
    private String hairColor;
    
    @NotNull(message = "Рост обязателен")
    @DecimalMin(value = "1", message = "Рост должен быть больше 0")
    @DecimalMax(value = "9223372036854775807", message = "Рост превышает допустимый диапазон Long")
    private BigInteger height;
    
    private String nationality;
    
    private Long locationId;
    
    private BigInteger locationX;
    private BigDecimal locationY;
    private BigInteger locationZ;
    private String locationName;
    
    @Valid
    private CreateLocationNested newLocation;
}

