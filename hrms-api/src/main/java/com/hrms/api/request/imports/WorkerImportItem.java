package com.hrms.api.request.imports;

import com.hrms.api.request.CreateWorkerRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WorkerImportItem extends CreateWorkerRequest implements ImportItem {
}

