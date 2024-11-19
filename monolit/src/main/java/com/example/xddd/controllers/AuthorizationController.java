package com.example.xddd.controllers;

import com.example.xddd.entities.User;
import com.example.xddd.repositories.RoleRepository;
import com.example.xddd.repositories.UserRepository;
import com.example.xddd.security.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "${cors.urls}")
@RestController
public class AuthorizationController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;


    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    RefreshTokenService refreshTokenService;

    @Autowired
    JwtUtil jwtUtil;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody ObjectNode json) {


        Authentication authentication;
        try {
            authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    json.get("login").asText(),
                                    json.get("password").asText()));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.ok().body("Invalid credentials");
        }

        final String token = jwtUtil.generateJwtToken(authentication);


        User user = userRepository.findByLogin(json.get("login").asText()).get();

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();
        ArrayNode arrayNode = objectMapper.createArrayNode();

        objectNode.put("id", user.getId());
        objectNode.put("token", token);

        List<Role> roles = user.getRole();

        for (Role role : roles) {
            arrayNode.add(role.getName().name());
        }

        objectNode.set("roles", arrayNode);

        objectNode.put("expires", jwtUtil.getExpirationDate(token).toInstant().toString());

        return ResponseEntity.ok().body(objectNode);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody ObjectNode json) {

        Optional<User> userOptional = userRepository.findByLogin(json.get("login")
                .asText());

        if (userOptional.isPresent()) {
            return ResponseEntity.status(401).body("Error: Phone is already taken!");
        }

        User user;

        user = new User(json.get("login").asText(),
                encoder.encode(json.get("password").asText()));
        user.setBalance(0L);

        user.getRole().add(roleRepository.findByName(ERole.ROLE_USER).get());

        userRepository.save(user);


        return ResponseEntity.ok().body("User registered successfully!");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(@RequestBody ObjectNode json) {
        refreshTokenService.deleteByUserId(json.get("user-id").asLong());
        return ResponseEntity.ok().body("Log out successful!");
    }
}
