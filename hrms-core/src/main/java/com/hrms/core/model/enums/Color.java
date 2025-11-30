package com.hrms.core.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Color {
    RED("Красный"),
    BLUE("Синий"),
    WHITE("Белый");

    private final String displayName;
}
