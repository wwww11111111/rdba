package com.example.xddd.services;

import com.example.xddd.entities.Cart;
import com.example.xddd.entities.Item;
import com.example.xddd.entities.Order;
import com.example.xddd.repositories.CartRepository;
import com.example.xddd.repositories.ItemsRepository;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ItemsServiceDev {

    private final ItemsRepository repository;
    private final CartRepository cartRepository;
    private final OrderService orderService;

    public ItemsServiceDev(ItemsRepository repository,
                        CartRepository cartRepository,
                        OrderService orderService) {
        this.repository = repository;
        this.cartRepository = cartRepository;
        this.orderService = orderService;
    }

    public ResponseEntity<?> getItems(Integer categoryId) {

        List<Item> items = new ArrayList<>();

        if (categoryId != null) {
            items.addAll(repository.findByCategoryId(categoryId));
        } else {
            repository.findAll().forEach(items::add);
        }
        return ResponseEntity.ok().body(items);
    }

    public ResponseEntity<?> purchaseItems(ObjectNode json) {

        Order order = validateOrderDetails(json);

        if (order == null) {
            return ResponseEntity.ok("Can not process order due to incorrect details");
        }

        String login = json.get("user").get("login").asText();

        List<Cart> carts = cartRepository
                .findCartsByOwnerLoginAndStatus(login, "reserved");

        for (Cart cart : carts) {
            Optional<Item> query = repository.findById(cart.getItemId());
            Cart alreadyPurchased = cartRepository
                    .findByOwnerLoginAndItemIdAndStatus(login, cart.getItemId(), "purchased");

            if (query.isPresent()) {
                Item item = query.get();

                if (item.getNumber() >= cart.getItemNumber()) {
                    item.setNumber(item.getNumber() - cart.getItemNumber());

                    if (alreadyPurchased != null) {
                        alreadyPurchased.setItemNumber(alreadyPurchased.getItemNumber() + cart.getItemNumber());
                    } else {
                        cart.setStatus("purchased");
                        cart.setOrderId(order.getId());
                    }
                    cartRepository.save(cart);
                    repository.save(item);
                } else return ResponseEntity.ok("Can not process order. We don't have some of your items");
            } else return ResponseEntity.ok("Can not process order. Cart is empty");
        }

        orderService.addOrder(order);

        return ResponseEntity.ok().build();


    }

    private Order validateOrderDetails(ObjectNode json) {

        Order order = new Order();

        String textDate;
        String region;
        String cityStreetHouse;
        int apartment;
        int floor;
        boolean lift;
        String phoneNumber;
        boolean paymentType;
        LocalDate date;
        boolean furnitureAssembly;

        try {
            textDate = json.get("delivery_date").asText();
            region = json.get("address").get("region").asText();
            cityStreetHouse = json.get("address").get("city_street_house").asText();
            apartment = Integer.parseInt(json.get("address").get("apartment").asText());
            floor = Integer.parseInt(json.get("address").get("floor").asText());
            lift = Boolean.parseBoolean(json.get("address").get("lift").asText());
            phoneNumber = json.get("address").get("phone_number").asText();
            paymentType = Boolean.parseBoolean(json.get("payment_type").asText());
            date = LocalDate.parse(textDate);
            furnitureAssembly = Boolean.parseBoolean(json.get("furniture_assembly").asText());

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }

        order.setDate(date);
        order.setRegion(region);
        order.setCityStreetHouse(cityStreetHouse);
        order.setApartment(apartment);
        order.setFloor(floor);
        order.setLift(lift);
        order.setPhoneNumber(phoneNumber);
        order.setTerminalPayment(paymentType);
        order.setFurnitureAssembly(furnitureAssembly);


        return order;
    }
}