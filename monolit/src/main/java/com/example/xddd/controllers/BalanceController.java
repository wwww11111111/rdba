package com.example.xddd.controllers;

import com.example.xddd.jms.JmsSender;
import com.example.xddd.services.BalanceService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class BalanceController {

    private final BalanceService balanceService;

    public BalanceController(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    @PostMapping("/fillUp")
    public ResponseEntity<?> fillUp(@RequestBody ObjectNode json) {
        return balanceService.fillUp(json);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(@RequestBody ObjectNode json) {
        return balanceService.withdraw(json);
    }
}
