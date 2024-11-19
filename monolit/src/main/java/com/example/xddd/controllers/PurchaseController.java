package com.example.xddd.controllers;

import com.example.xddd.services.PurchaseService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PurchaseController {

    private final PurchaseService service;

    public PurchaseController(PurchaseService service) {
        this.service = service;
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/purchase")
    public ResponseEntity<?> processOrder(@RequestBody ObjectNode json) {
        return service.process(json);
    }
}
