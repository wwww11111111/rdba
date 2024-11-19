package com.example.xddd.services;

import com.example.xddd.entities.Cart;
import com.example.xddd.entities.Item;
import com.example.xddd.entities.Order;
import com.example.xddd.entities.User;
import com.example.xddd.jms.JmsSender;
import com.example.xddd.repositories.CartRepository;
import com.example.xddd.repositories.ItemsRepository;
import com.example.xddd.repositories.OrderRepository;
import com.example.xddd.repositories.UserRepository;
import com.example.xddd.services.jobs.StatJob;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

//import javax.transaction.*;
import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;


@Service
public class ItemsService {

    @Getter
    @Setter
    private static int successfulOrders;

    private final ItemsRepository repository;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final JmsSender jmsSender;


    public ItemsService(ItemsRepository repository,
                        CartRepository cartRepository,
                        OrderRepository orderRepository, UserRepository userRepository, JmsSender jmsSender) {
        this.repository = repository;
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.jmsSender = jmsSender;
        successfulOrders = 0;
        try {
            startJob();
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }


    private void startJob() throws SchedulerException {

        SchedulerFactory schedulerFactory = new StdSchedulerFactory();
        Scheduler scheduler = schedulerFactory.getScheduler();
        JobDetail job = JobBuilder.newJob(StatJob.class)
                .withIdentity("myJob", "group1")
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("trigger3", "group1")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 * 8-17 * * ?"))
                .build();

        scheduler.scheduleJob(job, trigger);

        scheduler.start();

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

    public ResponseEntity<?> getItemById(Long itemId) {
        Optional<Item> item = repository.findById(itemId);
        if (item.isEmpty()) {
            return ResponseEntity.ok().body("No such item");
        }
        Item itemReal = item.get();
        return ResponseEntity.ok().body(itemReal);
    }

    public ResponseEntity<?> addItem(ObjectNode json) {
        Long id = json.get("id").asLong();
        int number = json.get("number").asInt();
        int categoryId = json.get("category_id").asInt();
        String description = json.get("description").asText();
        Long price = json.get("price").asLong();

        Item item = new Item(id, number, categoryId, description, price);

        item = repository.save(item);


        return ResponseEntity.ok().body(item);
    }

    public ResponseEntity<?> deleteItem(ObjectNode json) {

        Long id = json.get("id").asLong();

        Item toDelete = repository.findById(id).get();
        repository.delete(toDelete);
        return ResponseEntity.ok().body(toDelete);
    }


    private long calculateCart(List<Cart> carts) {

        long result = 0L;
        for (Cart cart : carts) {
            long price = repository.findById(cart.getItemId()).get().getPrice();
            result += cart.getItemNumber() * price;
        }

        return result;
    }


    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResponseEntity<?> purchaseItems(ObjectNode json) {
        User user = userRepository.findByLogin(
                SecurityContextHolder.getContext().getAuthentication().getName()
        ).get();

        Order order = validateOrderDetails(json);
        if (order == null) {
            return ResponseEntity.ok("Can not process order due to incorrect details");
        }


        List<Cart> carts = cartRepository
                .findCartsByOwnerLoginAndStatus(user.getLogin(), "reserved");

        order = orderRepository.save(order);
        long cartPrice = calculateCart(carts);


        for (Cart cart : carts) {
            Item item = repository.findById(cart.getItemId()).get();
            if (item.getNumber() >= cart.getItemNumber()) {
                item.setNumber(item.getNumber() - cart.getItemNumber());
                repository.save(item);
                cart.setStatus("waiting_payment");
                cart.setOrderId(order.getId());
                cartRepository.save(cart);
            } else {
                throw new RuntimeException("Can not process order. Not enough items");
            }
        }
        try {
            jmsSender.send(createWriteOffMessage(user.getId(), order.getId(), cartPrice),
                    "withdraw");
        } catch (Exception ignored) {
        }
        successfulOrders += 1;

        return ResponseEntity.ok().build();
    }


    //    @Transactional
    @Transactional
    @RabbitListener(queues = "withdrawAnswersQueue")
    public void withdrawAnswers(String message) {
        JSONObject jsonObject = new JSONObject(message);
        String status = (String) jsonObject.get("status");
        String orderIdText = (String) jsonObject.get("orderId");

        Order order = orderRepository.findById(Long.parseLong(orderIdText)).get();

        List<Cart> carts = cartRepository.findCartsByOrderId(order.getId());

        if (status.equals("success")) {
            carts.forEach(c -> {
                c.setStatus("acquired");
                cartRepository.save(c);
            });
        }

        if (status.equals("not enough balance")) {
            carts.forEach(c -> {
                c.setStatus("payment_issue");
                cartRepository.save(c);
                Item item = repository.findById(c.getItemId()).get();
                item.setNumber(item.getNumber() + c.getItemNumber());
                repository.save(item);
            });
        }

    }

    private String createWriteOffMessage(Long userId, Long orderId, Long amount) {
        return new JSONObject().put("userId", userId.toString())
                .put("orderId", orderId.toString())
                .put("amount", amount.toString())
                .toString();
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
        order.setStatus("pending");


        return order;
    }
}