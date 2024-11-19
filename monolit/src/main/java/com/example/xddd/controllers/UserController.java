package com.example.xddd.controllers;

import com.example.xddd.entities.User;
import com.example.xddd.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Autowired
    UserRepository userRepository;

    @GetMapping("/balance")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> getBalance() {

        User user = userRepository.findByLogin(
                SecurityContextHolder.getContext().getAuthentication().getName()
        ).get();

        return ResponseEntity.ok().body("{\"balance\": \"" + user.getBalance() + "\"}");

    }
}
