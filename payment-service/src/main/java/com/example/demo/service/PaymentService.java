package com.example.demo.service;

import com.example.demo.entities.PaidOrder;
import com.example.demo.entities.User;
import com.example.demo.jms.JmsSender;
import com.example.demo.repository.PaidOrderRepository;
import com.example.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONObject;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class PaymentService {


    @Autowired
    UserRepository userRepository;
    @Autowired
    PaidOrderRepository paidOrderRepository;

    private final JmsSender jmsSender;

    public PaymentService(JmsSender jmsSender) {
        this.jmsSender = jmsSender;
    }



    @Transactional
    @RabbitListener(queues = "fillUpQueue")
    public void fillBalance(String message) {
        JSONObject jsonObject;
        jsonObject = new JSONObject(message);
        String userId = (String) jsonObject.get("userId");
        Long paymentValue = Long.parseLong((String) jsonObject.get("amount"));
        User user = userRepository.findById(Long.valueOf(userId)).get();
        user.setBalance(
                user.getBalance() + paymentValue
        );
        user = userRepository.save(user);
    }

    private String buildNotEnoughBalanceMessage(Long orderId) {
        return new JSONObject().put("orderId", orderId.toString())
                .put("status", "not enough balance")
                .toString();
    }
    private String buildPaymentSuccessfulMessage(Long orderId) {
        return new JSONObject().put("orderId", orderId.toString())
                .put("status", "success")
                .toString();
    }

    private void sendNotEnoughBalance(Long orderId) throws MqttException {

        String message = buildNotEnoughBalanceMessage(orderId);
        jmsSender.send(message, "withdrawAnswers");
    }

    private void sendPaymentSuccessful(Long orderId) throws MqttException {
        String message = buildPaymentSuccessfulMessage(orderId);
        jmsSender.send(message, "withdrawAnswers");
    }

    @Transactional
    @RabbitListener(queues = "withdrawQueue")
    public void withdraw(String message) throws MqttException {

        JSONObject jsonObject;
        jsonObject = new JSONObject(message);
        String userIdText = (String) jsonObject.get("userId");
        String paymentValueText = (String) jsonObject.get("amount");
        String orderIdText = (String) jsonObject.get("orderId");

        Long userId = Long.parseLong(userIdText);
        Long paymentValue = Long.parseLong(paymentValueText);
        Long orderId = Long.parseLong(orderIdText);



        User user = userRepository.findById(Long.valueOf(userId)).get();
        if (user.getBalance() < paymentValue) {
            System.out.println("sending not enough");
            sendNotEnoughBalance(orderId);
            return;
        }

        user.setBalance(
                user.getBalance() - paymentValue
        );

        paidOrderRepository.save(new PaidOrder(orderId));

        userRepository.save(user);
        System.out.println("sending successful");
        sendPaymentSuccessful(orderId);
    }

    private String createWriteOffMessage(boolean success, long orderId) {
        return new JSONObject().put("success", Boolean.toString(success))
                .put("orderId", Long.toString(orderId))
                .toString();
    }

}
