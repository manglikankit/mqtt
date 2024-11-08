package com.demo.mqtt.mqtt;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class PublishSample {
    private MqttAsyncClient client;
    private MqttConnectOptions options;
    private ArrayList<String> unPublishedMsgs = new ArrayList<>();
    private static int counter = 1;
    public void mqttConnectionConfigue() throws MqttException {
        String broker = "tcp://broker.emqx.io:1883";
        String username = "emqx";
        String password = "public";
        String clientid = "publish_client";

        client = new MqttAsyncClient(broker, clientid);
        options = new MqttConnectOptions();
        options.setKeepAliveInterval(1000);
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        options.setAutomaticReconnect(true); // Automatically reconnect
        options.setCleanSession(false);

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                System.out.println("Connection lost! " + cause.getMessage());
                if(!client.isConnected())
                    reconnect();
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                System.out.println("Acknowledgment received Message: " + new String(message.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
              //  System.out.println("Delivery complete. Topic: " + token.getTopics()[0]);
            }
        });

        // Connect to the broker asynchronously
        IMqttToken token = client.connect(options);
        token.waitForCompletion(); // Wait until the connection is established
        System.out.println("Connected to MQTT broker");
        client.subscribe("mqtt/test_ack", 1);
    }

    public void publishMsg(String topic, String content) {
        if (!client.isConnected()) {
            System.err.println("Client is not connected. Cannot publish message.");
            unPublishedMsgs.add(String.format("%s__%s", topic, content));
            return;
        }

        MqttMessage message = new MqttMessage(content.getBytes());
        message.setQos(1);
        message.setRetained(true);

        try {
            client.publish(topic, message);
            System.out.println("Message published: " + content);
        } catch (MqttException e) {
            System.err.println("Failed to publish message: " + e.getMessage());
        }

    }
private synchronized void retryPublish(){
    Iterator<String> iterator = unPublishedMsgs.iterator();
    while(iterator.hasNext()){
        String next = iterator.next();
        String[] msgArr = next.split("__");
        publishMsg(msgArr[0], msgArr[1]);
        iterator.remove();
    }
}
    private void reconnect() {
        // Manual reconnection logic (optional)
        new Thread(() -> {
            while (!client.isConnected()) {
                try {
                    IMqttToken token = client.connect(options);
                    token.waitForCompletion(); // Wait until the connection is established
                    System.out.println("Reconnected to MQTT broker");
                    if(unPublishedMsgs.size() > 0)
                        retryPublish();
                } catch (MqttException e) {
                    System.err.println("Reconnection failed: " + e.getMessage());
                    try {
                        Thread.sleep(1000); // Wait before retrying
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }).start();
    }
    public static void main(String[] args) {
        PublishSample publisher = new PublishSample();
        try {
            publisher.mqttConnectionConfigue();
            Scanner sc = new Scanner(System.in);
            while (true) {
                System.out.println("Press Enter to publish a message or type 'exit' to quit:");
                String input = sc.nextLine();
                if (input.equalsIgnoreCase("exit")) {
                    break; // Exit loop
                }
                String localTime = LocalTime.now().toString();
                String topic = "mqtt/test";
                String content = String.format("This is test msg with time: %d", counter);
                publisher.publishMsg(topic, content); // Publish message
                counter += 1;
            }
            sc.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
//    public static void main(String[] args) {
//
//        String broker = "tcp://broker.emqx.io:1883";
//        String topic = "mqtt/test";
//        String username = "emqx";
//        String password = "public";
//        String clientid = "publish_client";
//        String localTime = LocalTime.now().toString();
//        String content = "rsyfxh etdhtdgh agd";
//
//        try {
//
//            MqttClient client = new MqttClient(broker, clientid, new MemoryPersistence());
//            MqttConnectOptions options = new MqttConnectOptions();
//            options.setUserName(username);
//            options.setPassword(password.toCharArray());
////            options.setConnectionTimeout(10); // Timeout for connection
//            options.setKeepAliveInterval(120); // Keep-alive interval (in seconds)
//            options.setAutomaticReconnect(true);
//            options.setCleanSession(false);
//            client.setCallback(new MqttCallback() {
//
//                @Override
//                public void connectionLost(Throwable cause) {
//                    // Called when the connection is lost
//                    System.out.println("Connection lost! " + cause.getMessage());
//                    if(!client.isConnected()) {
//                        try {
//                            client.connect();
//                            if(client.isConnected())
//                                System.out.println("reconnected");
//                        } catch (MqttException e) {
//                            System.out.println("couldn't reconnect");
//                        }
//                    }
//                }
//                @Override
//                public void messageArrived(String topic, MqttMessage message) throws Exception {
//                    // Called when a message arrives from the broker
//                   // System.out.println("Message arrived. Topic: " + topic + ", Message: " + new String(message.getPayload()));
//                }
//                @Override
//                public void deliveryComplete(IMqttDeliveryToken token) {
//                    // Called when a message has been delivered to the broker
//                    System.out.println("Delivery complete. Topic: " + token.getTopics()[0]);
//
//                    // System.out.println("Message: " + new String(token.getMessage().getPayload()));
//                }
//            });
//
//            // connect
//            client.connect(options);
//            // create message and setup QoS
//            MqttMessage message = new MqttMessage(content.getBytes());
//            message.setQos(1);
//            message.setRetained(true);
//            // publish message asynchronously
//            System.out.println("Enter any no");
//            Scanner sc = new Scanner(System.in);
//             sc.nextInt();
//            client.publish(topic, message);
//
//
////            CompletableFuture.runAsync(() -> {
////                try {
////                    client.publish(topic, message);
//////                    Thread.sleep(100000);
//////                    client.publish(topic, message);
////                    System.out.println("Message published asynchronously");
////                    System.out.println("Topic: " + topic);
////                } catch (Exception e) {
////                    e.printStackTrace();
////                }
////            });
//
//            client.subscribe("mqtt/test_ack", (topicAck, ackMessage) -> {
//                String ackPayload = new String(ackMessage.getPayload());
//                System.out.println("Received acknowledgment for message: " + ackPayload);
//            });
//
//            // disconnect
//          //  client.disconnect();
//            // close client
//            //client.close();
//        } catch (MqttPersistenceException e) {
//            throw new RuntimeException(e);
//        } catch (MqttSecurityException e) {
//            throw new RuntimeException(e);
//        } catch (MqttException e) {
//            throw new RuntimeException(e);
//        }
//
//
//
//    }

}