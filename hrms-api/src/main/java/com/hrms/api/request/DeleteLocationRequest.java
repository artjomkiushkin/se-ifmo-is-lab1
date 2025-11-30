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
public class DeleteLocationRequest {
    private List<PersonReplacement> replacements;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PersonReplacement {
        private Long personId;
        private Long newLocationId;
    }
}

