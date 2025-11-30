package com.hrms.api.controller;

import com.hrms.core.model.dto.WorkerDTO;
import com.hrms.service.service.WorkerService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/special")
@RequiredArgsConstructor
public class SpecialOperationsController {
    private final WorkerService workerService;
    
    @DeleteMapping("/workers/by-end-date")
    public ResponseEntity<Void> deleteWorkerByEndDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime endDate) {
        workerService.deleteWorkerByEndDate(endDate);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/workers/count-by-height")
    public ResponseEntity<Long> countWorkersByPersonHeight(@RequestParam Integer height) {
        return ResponseEntity.ok(workerService.countByPersonHeightGreaterThan(height));
    }
    
    @GetMapping("/workers/unique-start-dates")
    public ResponseEntity<List<Date>> getUniqueStartDates() {
        return ResponseEntity.ok(workerService.getUniqueStartDates());
    }
    
    @PostMapping("/workers/hire")
    public ResponseEntity<WorkerDTO> hireWorker(
            @RequestBody WorkerDTO workerDTO,
            @RequestParam Long organizationId) {
        return ResponseEntity.ok(workerService.hireWorkerToOrganization(workerDTO, organizationId));
    }
    
}

