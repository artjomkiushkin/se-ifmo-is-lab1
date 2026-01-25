package com.hrms.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrms.api.facade.ImportFacade;
import com.hrms.api.request.imports.ImportRequest;
import com.hrms.api.response.ImportOperationResponse;
import com.hrms.core.model.enums.ImportStatus;
import com.hrms.service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
public class ImportController {
    private final ImportFacade importFacade;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @PostMapping
    public ResponseEntity<ImportOperationResponse> importFile(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            var request = objectMapper.readValue(file.getInputStream(), ImportRequest.class);
            var user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            var result = importFacade.importEntities(request.getItems(), user.getId(), user.getUsername());
            
            if (result.getStatus() == ImportStatus.FAILED) {
                var statusCode = result.getErrorMessage() != null && 
                    result.getErrorMessage().contains("уже работает") 
                    ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;
                return ResponseEntity.status(statusCode).body(result);
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка чтения файла импорта: " + e.getMessage(), e);
        }
    }

    @GetMapping("/history")
    public ResponseEntity<Page<ImportOperationResponse>> getHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {
        var user = userService.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        
        var isAdmin = user.getRoles().contains("ROLE_ADMIN");
        
        if (isAdmin) {
            return ResponseEntity.ok(importFacade.getHistory(null, true, pageable));
        }
        
        return ResponseEntity.ok(importFacade.getHistory(user.getId(), false, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ImportOperationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(importFacade.getById(id));
    }
}

