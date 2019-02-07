package com.demo.java.mqdemo.activemq;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/6/27
 * <p>
 * Message Groups are an enhancement to the Exclusive Consumer feature. They provide:
 * <p>
 * Guaranteed ordering of the processing of related messages across a single queue.
 * Load balancing of the processing of messages across multiple consumers.
 * High availability / auto-failover to other consumers if a JVM goes down.
 * <p>
 * 提升独占消费模式的性能, 通过`JMSXGroupID`独占地消费, 而不需要所有消费都由一个消费者进行消费, 每个Group有序
 */
public class MessageGroup {

    private static final String USERNAME = "";
    private static final String PASSWORD = "";
    // failover集群模式
    private static final String FAILOVER_URLS = "failover:(tcp://47.98.191.186:61616,tcp://47.106.34.99:61616)";

    public static void main(String[] args) throws Exception {
        sendMessage();
        consumeMessage();
    }

    private static void sendMessage() throws Exception {
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(USERNAME, PASSWORD, FAILOVER_URLS);
        Connection connection = connectionFactory.createConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = session.createQueue("test-queue");
        MessageProducer messageProducer = session.createProducer(queue);
        messageProducer.setDeliveryMode(DeliveryMode.PERSISTENT);

        // 1.消息分组发送消息
        TextMessage textMessage = session.createTextMessage("hello, activemq!");
        // 设置消息分组
        textMessage.setStringProperty("JMSXGroupID", "IBM_NASDAQ_20/4/05");
        // 发送消息: send(Message message, int deliveryMode, int priority, long timeToLive)
        messageProducer.send(textMessage, DeliveryMode.PERSISTENT, 5, 60000L);

        // 2.关闭消息分组
        Message message = session.createTextMessage("message group will close!");
        // 设置消息分组
        message.setStringProperty("JMSXGroupID", "IBM_NASDAQ_20/4/05");
        // 设置负的序列号, 表示关闭分组
        message.setIntProperty("JMSXGroupSeq", -1);
        messageProducer.send(message);

        // 释放连接: 会自动释放Session等各种资源
        connection.close();
    }

    private static void consumeMessage() throws Exception {
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(USERNAME, PASSWORD, FAILOVER_URLS);
        Connection connection = connectionFactory.createConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = session.createQueue("test-queue");
        // 创建消费者
        MessageConsumer messageConsumer = session.createConsumer(queue);
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
