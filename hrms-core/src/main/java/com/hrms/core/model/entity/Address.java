package com.hrms.core.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldNameConstants;

@Entity
@Table(name = "addresses")
@Data
@Builder(toBuilder = true)
@With
@FieldNameConstants
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Почтовый индекс обязателен")
    @Column(name = "zip_code", nullable = false)
    private String zipCode;
}
