package com.hrms.api.request.imports;

import com.hrms.api.request.CreateLocationRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LocationImportItem extends CreateLocationRequest implements ImportItem {
}

