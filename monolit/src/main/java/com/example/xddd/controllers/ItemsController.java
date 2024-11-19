package com.example.xddd.controllers;

import com.example.xddd.services.ItemsService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class ItemsController {

    private final ItemsService service;

    public ItemsController(ItemsService service) {
        this.service = service;
    }

    @RequestMapping("/items")
    public ResponseEntity<?> items(@RequestParam(required = false) Integer categoryId,
                                   @RequestParam Map<String, String> params) {

        if (params.size() > 1) {
            return ResponseEntity.status(200).body("Unnecessary params has been passed");
        }
        return service.getItems(categoryId);
    }

    @RequestMapping("/getItemById")
    public ResponseEntity<?> getItemById(@RequestParam(required = true) Long itemId) {

        return service.getItemById(itemId);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/addItem")
    public ResponseEntity<?> addItem(@RequestBody ObjectNode json) {
        return service.addItem(json);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/deleteItem")
    public ResponseEntity<?> deleteItem(@RequestBody ObjectNode json) {
        return service.deleteItem(json);
    }
}
