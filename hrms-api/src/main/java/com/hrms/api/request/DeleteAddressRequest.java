package com.hrms.api.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteAddressRequest {
    private List<OrganizationReplacement> replacements;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrganizationReplacement {
        private Long organizationId;
        private Long newAddressId;
    }
}

