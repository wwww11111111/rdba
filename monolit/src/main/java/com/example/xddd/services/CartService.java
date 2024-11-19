package com.example.xddd.services;

import com.example.xddd.entities.Cart;
import com.example.xddd.entities.Item;
import com.example.xddd.entities.User;
import com.example.xddd.repositories.CartRepository;
import com.example.xddd.repositories.ItemsRepository;
import com.example.xddd.repositories.UserRepository;
import com.example.xddd.security.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.transaction.*;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final ItemsRepository itemsRepository;
    private final CartRepository repository;
    private final UserRepository userRepository;


    public CartService(ItemsRepository itemsRepository, CartRepository repository, UserRepository userRepository) {
        this.itemsRepository = itemsRepository;
        this.repository = repository;
        this.userRepository = userRepository;
    }


    public ResponseEntity<?> add(ObjectNode json) {

        User user = userRepository.findByLogin(
                SecurityContextHolder.getContext().getAuthentication().getName()
        ).get();


        int number;
        try {
            number = Integer.parseInt(
                    json.get("number").asText()
            );
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Missing some required parameters");
        }

        long id;

        try {
            id = Long.parseLong(
                    json.get("id").asText()
            );
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Missing some required parameters");

        }

        Optional<Item> itemOptional = itemsRepository.findById(id);

        if (itemOptional.isEmpty()) {
            return ResponseEntity.ok().body("No item with such id exists");
        }

        Cart cart = repository.findByOwnerLoginAndItemIdAndStatus(
                user.getLogin(), id, "reserved"
        );

        if (number < 1) {
            return ResponseEntity.ok().body("you should add at least 1 item");
        }

        if (cart == null) {

            cart = new Cart(user.getLogin(),
                    id, number, "reserved", null);
            repository.save(cart);
        } else {
            cart.setItemNumber(cart.getItemNumber() + number);
            repository.save(cart);
        }

        ObjectMapper objectMapper = new ObjectMapper();

        ObjectNode objectNode = objectMapper.valueToTree(cart);

        objectNode.remove("orderId");

        return ResponseEntity.ok().body(objectNode);
    }


    public ResponseEntity<?> delete(ObjectNode json) {
        User user = userRepository.findByLogin(
                SecurityContextHolder.getContext().getAuthentication().getName()
        ).get();

        int id = Integer.parseInt(
                json.get("id").asText()
        );

        Cart cart = repository.findByOwnerLoginAndItemIdAndStatus(
                user.getLogin(), id, "reserved"
        );

        if (cart != null) {
            repository.delete(cart);
        }

        return ResponseEntity.ok().build();
    }


    public ResponseEntity<?> getCart(ObjectNode json) {
        User user = userRepository.findByLogin(
                SecurityContextHolder.getContext().getAuthentication().getName()
        ).get();

        List<Cart> items = repository.findCartsByOwnerLoginAndStatus(user.getLogin(), "reserved");

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode root = objectMapper.createObjectNode();

        ArrayNode array = objectMapper.createArrayNode();

        for (Cart item : items) {

            String description = itemsRepository.findById(item.getItemId()).get().getDescription();

            ObjectNode newNode;

            newNode = ((ObjectNode) objectMapper.valueToTree(item)).put("description",
                    description);

            newNode.remove("orderId");

            array.add(newNode);
        }


        root.set("items", array);
        return ResponseEntity.ok().body(root);
    }


}
