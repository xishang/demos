package com.demo.java.mqdemo.activemq;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/6/21
 */
public class ActivemqDemo {

    private static final String USERNAME = "";
    private static final String PASSWORD = "";
    private static final String BROKER_URL = "tcp://47.98.191.186:61616";

    // failover集群模式
    private static final String FAILOVER_URLS = "failover:(tcp://47.98.191.186:61616,tcp://47.106.34.99:61616)";

    static {
        System.out.println("ActivemqDemo static block!");
    }

    public static void main(String[] args) throws Exception {
//        sendMessage();
        consumeMessage();
    }

    private static void sendMessage() throws Exception {
        // 创建连接工厂
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(USERNAME, PASSWORD, FAILOVER_URLS);
        // 创建连接, 默认是关闭的
        Connection connection = connectionFactory.createConnection();
        // 开启连接
        connection.start();
        // 创建Session
        // 是否开启事务: false
        // 应答模式: 自动应答
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        // 创建Destination: Queue or Topic
        Destination queue = session.createQueue("some-queue");
        // 创建生产者
        MessageProducer messageProducer = session.createProducer(queue);
        // 设置持久化/非持久化
        messageProducer.setDeliveryMode(DeliveryMode.PERSISTENT);
        // 创建消息
        TextMessage textMessage = session.createTextMessage();
        textMessage.setText("hello, activemq!");
        // 发送消息: send(Message message, int deliveryMode, int priority, long timeToLive)
        messageProducer.send(textMessage, DeliveryMode.PERSISTENT, 5, 60000L);
        // 释放连接: 会自动释放Session等各种资源
        connection.close();
    }

    private static void consumeMessage() throws Exception {
        // 创建连接工厂
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(USERNAME, PASSWORD, FAILOVER_URLS);
        // 创建连接, 默认是关闭的
        Connection connection = connectionFactory.createConnection();
        // 设置ClientID
        connection.setClientID("consumer-01");
        // 开启连接
        connection.start();
        // 创建Session
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        // 创建Destination
        Destination queue = session.createQueue("some-queue");
        // 创建消费者
        MessageConsumer messageConsumer = session.createConsumer(queue);
        // 同步消费
        TextMessage syncMessage = (TextMessage) messageConsumer.receive();
        System.out.println("同步消费: " + syncMessage.getText());
        // 异步消费: 设置消息监听
        messageConsumer.setMessageListener(message -> {
            TextMessage asyncMessage = (TextMessage) message;
            try {
                System.out.println("异步: " + asyncMessage.getText());
            } catch (JMSException e) {
            }
        });
        // 释放连接: 会自动释放Session等各种资源
        connection.close();
    }

}
