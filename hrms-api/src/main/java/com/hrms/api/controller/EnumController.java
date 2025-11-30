package com.hrms.api.controller;

import com.hrms.api.response.EnumValueResponse;
import com.hrms.core.model.enums.Color;
import com.hrms.core.model.enums.Country;
import com.hrms.core.model.enums.OrganizationType;
import com.hrms.core.model.enums.Position;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/enums")
@RequiredArgsConstructor
public class EnumController {

    @GetMapping("/colors")
    public List<EnumValueResponse> getColors() {
        return Arrays.stream(Color.values())
            .map(e -> new EnumValueResponse(e.name(), e.getDisplayName()))
            .toList();
    }

    @GetMapping("/countries")
    public List<EnumValueResponse> getCountries() {
        return Arrays.stream(Country.values())
            .map(e -> new EnumValueResponse(e.name(), e.getDisplayName()))
            .toList();
    }

    @GetMapping("/organization-types")
    public List<EnumValueResponse> getOrganizationTypes() {
        return Arrays.stream(OrganizationType.values())
            .map(e -> new EnumValueResponse(e.name(), e.getDisplayName()))
            .toList();
    }

    @GetMapping("/positions")
    public List<EnumValueResponse> getPositions() {
        return Arrays.stream(Position.values())
            .map(e -> new EnumValueResponse(e.name(), e.getDisplayName()))
            .toList();
    }

    @GetMapping
    public Map<String, List<EnumValueResponse>> getAllEnums() {
        return Map.of(
            "colors", getColors(),
            "countries", getCountries(),
            "organizationTypes", getOrganizationTypes(),
            "positions", getPositions()
        );
    }
}

