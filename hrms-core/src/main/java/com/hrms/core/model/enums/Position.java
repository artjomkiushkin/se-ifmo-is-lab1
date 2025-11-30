package com.hrms.core.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Position {
    DIRECTOR("Директор"),
    MANAGER("Менеджер"),
    HUMAN_RESOURCES("HR"),
    HEAD_OF_DIVISION("Руководитель отдела"),
    CLEANER("Уборщик");

    private final String displayName;
}
