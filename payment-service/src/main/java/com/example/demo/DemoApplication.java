package com.example.demo;

import org.eclipse.paho.client.mqttv3.*;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.util.UUID;


@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) throws MqttException, IOException {
        SpringApplication.run(DemoApplication.class, args);

    }
}
