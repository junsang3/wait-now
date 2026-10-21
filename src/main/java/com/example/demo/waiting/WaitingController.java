package com.example.demo.waiting;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class WaitingController {
    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping("/stores/{storeId}/waitings")
    public ResponseEntity<Void> create(@PathVariable Long storeId) {
        waitingService.issue(storeId);
        return ResponseEntity.ok().build();
    }
}
