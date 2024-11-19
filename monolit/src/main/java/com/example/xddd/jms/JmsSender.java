package com.example.xddd.jms;

import org.eclipse.paho.client.mqttv3.*;
import org.springframework.stereotype.Component;

@Component
public class JmsSender {

    MqttAsyncClient aClient = new MqttAsyncClient("tcp://localhost:1883",
            "testClientLmao3", null);
    MqttConnectOptions options = new MqttConnectOptions();

    public JmsSender() throws MqttException {

        aClient.connect(options, new IMqttActionListener() {
            public void onSuccess(IMqttToken asyncActionToken) {
                System.out.println("Connected");
            }

            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                System.out.println("Connection failed: " + exception);
            }
        });
    }


    public void send(String message, String topic) throws MqttException {
        MqttMessage msg = new MqttMessage(message.getBytes());
        msg.setRetained(true);
        aClient.publish(topic, msg);
    }
}
