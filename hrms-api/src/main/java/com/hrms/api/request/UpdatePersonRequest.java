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
public class UpdatePersonRequest {
    @NotBlank(message = "Имя обязательно")
    private String name;
    
    @NotBlank(message = "Цвет глаз обязателен")
    private String eyeColor;
    
    @NotBlank(message = "Цвет волос обязателен")
    private String hairColor;
    
    private Long locationId;
    
    @DecimalMax(value = "9223372036854775807", message = "Координата X превышает допустимый диапазон Long")
    private BigInteger locationX;
    
    @DecimalMax(value = "1.7976931348623157E308", message = "Координата Y превышает допустимый диапазон Double")
    private BigDecimal locationY;
    
    @DecimalMax(value = "9223372036854775807", message = "Координата Z превышает допустимый диапазон Long")
    private BigInteger locationZ;
    
    private String locationName;
    
    @NotNull(message = "Рост обязателен")
    @DecimalMin(value = "1", message = "Рост должен быть больше 0")
    @DecimalMax(value = "9223372036854775807", message = "Рост превышает допустимый диапазон Long")
    private BigInteger height;
    
    private String nationality;
}
