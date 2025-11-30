package com.hrms.core.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Country {
    UNITED_KINGDOM("Великобритания"),
    USA("США"),
    SPAIN("Испания"),
    CHINA("Китай"),
    NORTH_KOREA("Северная Корея");

    private final String displayName;
}
