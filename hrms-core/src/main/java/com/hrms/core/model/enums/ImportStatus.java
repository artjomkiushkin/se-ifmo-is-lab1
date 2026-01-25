package com.hrms.core.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ImportStatus {
    IN_PROGRESS("В процессе"),
    SUCCESS("Успешно"),
    FAILED("Ошибка");

    private final String displayName;
}

