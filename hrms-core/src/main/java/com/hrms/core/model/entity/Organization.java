package com.hrms.core.model.entity;

import com.hrms.core.model.enums.OrganizationType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.FieldNameConstants;

@Entity
@Table(name = "organizations")
@NamedEntityGraph(
    name = "Organization.full",
    attributeNodes = {
        @NamedAttributeNode("officialAddress")
    }
)
@Data
@Builder(toBuilder = true)
@With
@FieldNameConstants
@NoArgsConstructor
@AllArgsConstructor
public class Organization {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "official_address_id")
    private Address officialAddress;

    @Positive(message = "Годовой оборот должен быть больше 0")
    @Column(name = "annual_turnover", nullable = false)
    private float annualTurnover;

    @Positive(message = "Количество сотрудников должно быть больше 0")
    @Column(name = "employees_count")
    private Long employeesCount;

    @NotBlank(message = "Полное название обязательно")
    @Column(name = "full_name", nullable = false)
    private String fullName;

    @NotNull(message = "Рейтинг обязателен")
    @Positive(message = "Рейтинг должен быть больше 0")
    @Column(nullable = false)
    private Float rating;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private OrganizationType type;
}
