package com.example.xddd.services;

import com.example.xddd.entities.User;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class PurchaseService {

    private final ItemsService itemsService;

    public PurchaseService(ItemsService itemsService) {
        this.itemsService = itemsService;
    }

    public ResponseEntity<?> process(ObjectNode json) {
        ResponseEntity<?> response;
        try {
            response = itemsService.purchaseItems(json);
        } catch (RuntimeException e) {
            return ResponseEntity.ok().body(e.getMessage());
        }
        return response;
    }

}
