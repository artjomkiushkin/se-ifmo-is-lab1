package com.hrms.api.request.imports;

import com.hrms.api.request.CreateAddressRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AddressImportItem extends CreateAddressRequest implements ImportItem {
}

