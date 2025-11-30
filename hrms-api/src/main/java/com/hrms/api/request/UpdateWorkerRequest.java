package com.hrms.api.request;

import com.hrms.api.request.nested.CreateOrganizationNested;
import com.hrms.api.request.nested.CreatePersonNested;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateWorkerRequest {
    @NotNull(message = "Координата X обязательна")
    @DecimalMax(value = "1.7976931348623157E308", message = "Координата X превышает допустимый диапазон Double")
    private BigDecimal coordinatesX;
    
    @NotNull(message = "Координата Y обязательна")
    @DecimalMax(value = "3.4028235E38", message = "Координата Y превышает допустимый диапазон Float")
    private BigDecimal coordinatesY;
    
    private Long organizationId;
    
    @Valid
    private CreateOrganizationNested editOrganization;
    
    @NotNull(message = "Зарплата обязательна")
    @DecimalMin(value = "0", inclusive = false, message = "Зарплата должна быть больше 0")
    @DecimalMax(value = "1.7976931348623157E308", message = "Зарплата превышает допустимый диапазон Double")
    private BigDecimal salary;
    
    @NotNull(message = "Рейтинг обязателен")
    @DecimalMin(value = "0", inclusive = false, message = "Рейтинг должен быть больше 0")
    @DecimalMax(value = "3.4028235E38", message = "Рейтинг превышает допустимый диапазон Float")
    private BigDecimal rating;
    
    @NotNull(message = "Дата начала работы обязательна")
    private Date startDate;
    
    private String endDate;
    
    @NotBlank(message = "Должность обязательна")
    private String position;
    
    private Long personId;
    
    @Valid
    private CreatePersonNested editPerson;
}
