package com.hrms.api.request.imports;

import com.hrms.api.request.CreateOrganizationRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrganizationImportItem extends CreateOrganizationRequest implements ImportItem {
}

