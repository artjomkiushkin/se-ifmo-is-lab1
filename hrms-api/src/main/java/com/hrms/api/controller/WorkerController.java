package com.hrms.api.controller;

import com.hrms.api.facade.WorkerFacade;
import com.hrms.api.request.BatchUpdateWorkerRequest;
import com.hrms.api.request.CreateWorkerRequest;
import com.hrms.api.request.UpdateWorkerRequest;
import com.hrms.api.response.WorkerResponse;
import com.hrms.core.model.dto.FilterDTO;
import com.hrms.service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/workers")
@RequiredArgsConstructor
public class WorkerController {
    
    private final WorkerFacade workerFacade;
    private final NotificationService notificationService;
    
    @GetMapping
    public ResponseEntity<Page<WorkerResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(workerFacade.getAllWorkers(pageable));
    }
    
    @PostMapping("/filter")
    public ResponseEntity<Page<WorkerResponse>> filter(@RequestBody List<FilterDTO> filters, Pageable pageable) {
        return ResponseEntity.ok(workerFacade.filterWorkers(filters, pageable));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<WorkerResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(workerFacade.getWorkerById(id));
    }
    
    @PostMapping
    public ResponseEntity<WorkerResponse> create(@Valid @RequestBody CreateWorkerRequest request) {
        var created = workerFacade.createWorker(request);
        return ResponseEntity.ok(created);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<WorkerResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateWorkerRequest request) {
        var updated = workerFacade.updateWorker(id, request);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        workerFacade.deleteWorker(id);
        notificationService.notifyWorkerDeleted(id);
        return ResponseEntity.noContent().build();
    }
    
    @PatchMapping("/batch")
    public ResponseEntity<List<WorkerResponse>> batchUpdate(@Valid @RequestBody List<BatchUpdateWorkerRequest> requests) {
        return ResponseEntity.ok(workerFacade.batchUpdate(requests));
    }
}

