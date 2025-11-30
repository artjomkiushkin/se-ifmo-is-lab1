package com.hrms.api.facade;

import com.hrms.api.mapper.WorkerFacadeMapper;
import com.hrms.api.request.BatchUpdateWorkerRequest;
import com.hrms.api.request.CreateWorkerRequest;
import com.hrms.api.request.UpdateWorkerRequest;
import com.hrms.api.response.WorkerResponse;
import com.hrms.core.model.dto.FilterDTO;
import com.hrms.service.service.WorkerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import one.util.streamex.StreamEx;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WorkerFacade {
    private final WorkerService workerService;
    private final WorkerFacadeMapper mapper;

    public Page<WorkerResponse> getAllWorkers(Pageable pageable) {
        return workerService.findAll(pageable).map(mapper::toResponse);
    }

    public Page<WorkerResponse> filterWorkers(List<FilterDTO> filters, Pageable pageable) {
        return workerService.findByFilters(filters, pageable).map(mapper::toResponse);
    }

    public WorkerResponse getWorkerById(Long id) {
        return mapper.toResponse(workerService.findById(id));
    }

    public WorkerResponse createWorker(CreateWorkerRequest request) {
        return mapper.toResponse(workerService.createWithNested(mapper.toCreateDTO(request)));
    }

    public WorkerResponse updateWorker(Long id, UpdateWorkerRequest request) {
        if (request.getEditOrganization() != null || request.getEditPerson() != null) {
            var dto = mapper.toUpdateDTO(request);
            dto.setOrganizationId(request.getOrganizationId());
            dto.setPersonId(request.getPersonId());
            return mapper.toResponse(workerService.updateWithNested(id, dto));
        }
        return mapper.toResponse(workerService.update(id, mapper.toDTO(request)));
    }

    public void deleteWorker(Long id) {
        workerService.delete(id);
    }

    public List<WorkerResponse> batchUpdate(List<BatchUpdateWorkerRequest> requests) {
        if (requests.isEmpty()) {
            return List.of();
        }
        var first = requests.get(0);
        if (first.getOrganizationId() != null) {
            var map = StreamEx.of(requests).toMap(BatchUpdateWorkerRequest::getId, BatchUpdateWorkerRequest::getOrganizationId);
            return StreamEx.of(workerService.batchUpdateOrganization(map)).map(mapper::toResponse).toList();
        } else {
            var map = StreamEx.of(requests).toMap(BatchUpdateWorkerRequest::getId, BatchUpdateWorkerRequest::getPersonId);
            return StreamEx.of(workerService.batchUpdatePerson(map)).map(mapper::toResponse).toList();
        }
    }
}
