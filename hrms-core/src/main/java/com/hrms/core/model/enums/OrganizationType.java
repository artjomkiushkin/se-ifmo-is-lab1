package com.hrms.core.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrganizationType {
    COMMERCIAL("Коммерческая"),
    PUBLIC("Государственная"),
    TRUST("Трастовая"),
    OPEN_JOINT_STOCK_COMPANY("ОАО");

    private final String displayName;
}
