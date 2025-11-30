package com.hrms.core.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Coordinates {
    @NotNull
    @Column(name = "coordinate_x", nullable = false)
    private Double x;

    @NotNull
    @Column(name = "coordinate_y", nullable = false)
    private Float y;
}

