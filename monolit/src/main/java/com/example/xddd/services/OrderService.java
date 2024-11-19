package com.example.xddd.services;

import com.example.xddd.entities.Order;
import com.example.xddd.repositories.OrderRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }

    public ResponseEntity<?> addOrder(Order order) {
        repository.save(order);

        return ResponseEntity.ok().build();
    }
}
