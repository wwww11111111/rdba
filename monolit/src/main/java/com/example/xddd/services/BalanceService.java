package com.example.xddd.services;

import com.example.xddd.entities.User;
import com.example.xddd.jms.JmsSender;
import com.example.xddd.repositories.UserRepository;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class BalanceService {

    private final JmsSender jmsSender;
    private final UserRepository userRepository;


    public BalanceService(JmsSender jmsSender, UserRepository userRepository) {
        this.jmsSender = jmsSender;
        this.userRepository = userRepository;
    }

    private String buildFillUpMessage(long userId, long amount) {

        return new JSONObject().put("userId", Long.toString(userId))
                .put("amount", Long.toString(amount)).toString();
    }


    public ResponseEntity<?> fillUp(ObjectNode json) {


        long amount = Long.parseLong(json.get("amount").asText());

        User user = userRepository.findByLogin(
                SecurityContextHolder.getContext().getAuthentication().getName()
        ).get();

        String message = buildFillUpMessage(user.getId(), amount);

        try {
            jmsSender.send(message, "fillUp");
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> withdraw(ObjectNode json) {


        long amount = Long.parseLong(json.get("amount").asText());

        User user = userRepository.findByLogin(
                SecurityContextHolder.getContext().getAuthentication().getName()
        ).get();

        String message = buildFillUpMessage(user.getId(), amount);
        try {
            jmsSender.send(message, "withdraw");
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok().build();
    }

}
