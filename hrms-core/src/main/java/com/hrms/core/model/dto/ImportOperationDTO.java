package com.hrms.core.model.dto;

import com.hrms.core.model.enums.ImportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportOperationDTO {
    private Long id;
    private ImportStatus status;
    private Long userId;
    private String username;
    private Integer addedCount;
    private String errorMessage;
    private ZonedDateTime createdAt;
    private ZonedDateTime finishedAt;
}

