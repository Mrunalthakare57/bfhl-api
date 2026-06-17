package com.bfhl.controller;

import com.bfhl.dto.BfhlRequest;
import com.bfhl.dto.BfhlResponse;
import com.bfhl.service.BfhlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class BfhlController {

    private final BfhlService bfhlService;

    @PostMapping("/bfhl")
    public ResponseEntity<BfhlResponse> processData(
            @Valid @RequestBody BfhlRequest request,
            @RequestHeader(value = "X-Request-Id", required = false) String requestId) {

        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }
        log.info("POST /bfhl X-Request-Id={}", requestId);
        BfhlResponse response = bfhlService.processData(request, requestId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "UP");
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("service", "bfhl-api");
        return ResponseEntity.ok(body);
    }
}
