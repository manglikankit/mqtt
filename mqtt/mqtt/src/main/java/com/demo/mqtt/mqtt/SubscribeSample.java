package com.demo.mqtt.mqtt;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class SubscribeSample {
    public static void main(String[] args) {
        String broker = "tcp://broker.emqx.io:1883";
        String topic = "mqtt/test";
        String username = "emqx";
        String password = "public";
        String clientid = "subscribe_client";
        int qos = 1;

        try {
            MqttClient client = new MqttClient(broker, clientid, new MemoryPersistence());
            // connect options
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(username);
            options.setPassword(password.toCharArray());
            options.setAutomaticReconnect(true);
            options.setCleanSession(false);
            // setup callback
            client.setCallback(new MqttCallback() {

                private String s;
                private MqttMessage mqttMessage;
                public void connectionLost(Throwable cause) {
                    System.out.println("connectionLost: " + cause.getMessage());
                }
                @Override
                public void messageArrived(String topic, MqttMessage message) throws MqttException {
//                    System.out.println("topic: " + topic);
//                    System.out.println("Qos: " + message.getQos());
                    System.out.println("message received: " + new String(message.getPayload()));
                    /*
                    String ackPayload = "Acknowledged message: " + message;
                    MqttMessage ackMessage = new MqttMessage(ackPayload.getBytes());
                    ackMessage.setQos(1);  // Set QoS for acknowledgment as well
                    client.publish("mqtt/test_ack", ackMessage);
                    System.out.println("Acknowledgment sent: " + ackPayload);
                     */


                }
                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                   // System.out.println("deliveryComplete---------" + token.isComplete());
                }
            });
            client.connect(options);
            client.subscribe(topic, qos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
