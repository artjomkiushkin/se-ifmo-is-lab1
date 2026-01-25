package com.hrms.api.request.imports;

import com.hrms.api.request.CreatePersonRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PersonImportItem extends CreatePersonRequest implements ImportItem {
}

