package com.hrms.core.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldNameConstants;

@Entity
@Table(name = "locations")
@Data
@Builder(toBuilder = true)
@With
@FieldNameConstants
@NoArgsConstructor
@AllArgsConstructor
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private long x;

    @Column(nullable = false)
    private double y;

    @NotNull(message = "Координата Z обязательна")
    @Column(nullable = false)
    private Long z;

    @Column(name = "name")
    private String name;
}
