package com.hrms.api.request.imports;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = WorkerImportItem.class, name = "worker"),
    @JsonSubTypes.Type(value = OrganizationImportItem.class, name = "organization"),
    @JsonSubTypes.Type(value = PersonImportItem.class, name = "person"),
    @JsonSubTypes.Type(value = LocationImportItem.class, name = "location"),
    @JsonSubTypes.Type(value = AddressImportItem.class, name = "address")
})
public interface ImportItem {
}

