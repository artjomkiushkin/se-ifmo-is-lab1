package com.hrms.core.model.entity;

import com.hrms.core.model.enums.Color;
import com.hrms.core.model.enums.Country;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.FieldNameConstants;

@Entity
@Table(name = "persons")
@NamedEntityGraph(
    name = "Person.full",
    attributeNodes = {
        @NamedAttributeNode("location")
    }
)
@Data
@Builder(toBuilder = true)
@With
@FieldNameConstants
@NoArgsConstructor
@AllArgsConstructor
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Имя обязательно")
    @Column(nullable = false)
    private String name;

    @NotNull(message = "Цвет глаз обязателен")
    @Enumerated(EnumType.STRING)
    @Column(name = "eye_color", nullable = false)
    private Color eyeColor;

    @NotNull(message = "Цвет волос обязателен")
    @Enumerated(EnumType.STRING)
    @Column(name = "hair_color", nullable = false)
    private Color hairColor;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "location_id")
    private Location location;

    @Positive(message = "Рост должен быть больше 0")
    @Column(nullable = false)
    private long height;

    @Enumerated(EnumType.STRING)
    @Column(name = "nationality")
    private Country nationality;
}
