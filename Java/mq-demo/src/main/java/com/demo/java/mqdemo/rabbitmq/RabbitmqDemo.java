package com.demo.java.mqdemo.rabbitmq;

import com.rabbitmq.client.*;

import javax.jms.DeliveryMode;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/6/21
 */
public class RabbitmqDemo {

    private static final String USERNAME = "";
    private static final String PASSWORD = "";
    private static final String BROKER_URL = "";

    public static void main(String[] args) throws Exception {
        sendMessage();
        consumeMessage();
    }

    public static Connection createConnection() throws Exception {
        // 创建连接工厂: RabbitMQ不使用JMS的ConnectionFactory和Connection
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setUsername(USERNAME);
        connectionFactory.setPassword(PASSWORD);
        connectionFactory.setHost(BROKER_URL);
        // 创建连接
        Connection connection = connectionFactory.newConnection();
        return connection;
    }

    public static void sendMessage() throws Exception {
        // 创建连接
        Connection connection = createConnection();
        // 创建channel
        Channel channel = connection.createChannel();
        String exchangeName = "test-exchange";
        /* 声明交换器: exchangeDeclare(String exchangeName, String type, boolean durable, boolean autoDelete, boolean internal, Map<String, Object> arguments)
         -> exchangeName: 交换器名
         -> type: 交换器类型: fanout, direct, topic, headers等
         -> durable: 是否持久化(存储到磁盘)
         -> autoDelete: 是否自动删除
         -> internal: 是否使用内置的RabbitMQ
         -> arguments: 其他参数(如设置备用交换器)
         */
        channel.exchangeDeclare(exchangeName, "direct", true, false, false, null);
        String queueName = "test-queue";
        /* 声明队列: queueDeclare(String queueName, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments)
         -> queueName: 队列名
         -> durable: 是否持久化
         -> exclusive: 是否是排他队列(只对首次声明该队列的连接可见)
         -> autoDelete: 是否自动删除
         -> arguments: 其他参数(如设置过期时间)
         */
        channel.queueDeclare(queueName, true, false, false, null);
        String routingKey = "test-binding";
        /* queue和exchange绑定: queueBind(String queueName, String exchangeName, String routingKey, Map<String, Object> arguments)
         -> queueName: 队列名
         -> exchangeName: 交换器名
         -> routingKey: 绑定的key
         -> arguments: 其他参数
         */
        channel.queueBind(queueName, exchangeName, routingKey, null);
        // 发布消息
        byte[] messageBytes = "hello, rabbitmq!".getBytes();
        channel.basicPublish(exchangeName, routingKey, null, messageBytes);
        // 关闭资源
        channel.close();
        connection.close();
    }

    public static void consumeMessage() throws Exception {
        // 创建连接
        Connection connection = createConnection();
        // 创建channel
        Channel channel = connection.createChannel();
        String queueName = "test-queue";
        /* 消费消息(推模式): basicConsume(String queueName, boolean autoAck, String consumerTag, boolean noLocal, boolean exclusive, Map<String, Object> arguments, Consumer callback)
         -> queueName: 队列名
         -> autoAck: 是否自动确认, 设置为false以避免不必要的消息丢失
         -> consumerTag: 消费者标签, 用于区分不同的消费者
         -> noLocal: 是否允许本地消费(即消费本地生产者发布的消息)
         -> exclusive: 是否是排他模式
         -> arguments: 其他参数
         -> callback: 异步消费回调
         */
        channel.basicConsume(queueName, false, "test-consumer", false, false, null, new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                System.out.println("RabbitMQ 异步消费: " + new String(body, Charset.forName("utf-8")));
                // 主动确认消息: basicAck(long deliveryTag, boolean multiple)
                channel.basicAck(envelope.getDeliveryTag(), false);
            }
        });
        /* 消费消息(拉模式): basicGet(String queueName, boolean autoAck)
         -> queueName: 队列名
         -> autoAck: 是否自动确认, 设置为false以避免不必要的消息丢失
         */
        GetResponse message = channel.basicGet(queueName, false);
        System.out.println("RabbitMQ 同步消费: " + new String(message.getBody(), Charset.forName("utf-8")));
        // 主动确认消息
        channel.basicAck(message.getEnvelope().getDeliveryTag(), false);
        // 关闭资源
        channel.close();
        connection.close();
    }

    /**
     * 备份交换器
     */
    public static void alternateExchange() throws Exception {
        // 创建连接
        Connection connection = createConnection();
        // 创建channel
        Channel channel = connection.createChannel();
        String alternateExchange = "test-alternate";
        // 声明备份交换器: type为"fanout", 表示任何消息转发到队列
        channel.exchangeDeclare(alternateExchange, "fanout", true, false, null);
        String alternateQueue = "alternate-queue";
        // 声明备份交换器绑定的队列
        channel.queueDeclare(alternateQueue, true, false, false, null);
        // 绑定备份交换器和队列
        channel.queueBind(alternateQueue, alternateExchange, "");

        // 交换器参数
        Map<String, Object> arguments = new HashMap<>();
        // === 设置备份交换器
        arguments.put("alternate-exchange", alternateExchange);
        String normalExchange = "normal-exchange";
        // 声明正常使用的交换器
        channel.exchangeDeclare(normalExchange, "direct", true, false, arguments);
        String normalQueue = "normal-queue";
        // 声明队列
        channel.queueDeclare(normalQueue, true, false, false, null);
        // 绑定正常交换器和队列
        channel.queueBind(normalQueue, normalExchange, "normal-key");

        // 发布消息
        byte[] messageBytes = "routing failed!".getBytes();
        // 向正常交换器发送消息, 由于routingKey不匹配, 将会路由到备份交换器
        channel.basicPublish(normalExchange, "miss-key", null, messageBytes);
        // 关闭资源
        channel.close();
        connection.close();
    }

    /**
     * TTL, 死信队列, 延迟队列, 优先级队列
     */
    public static void ttl() throws Exception {
        // 创建连接
        Connection connection = createConnection();
        // 创建channel
        Channel channel = connection.createChannel();
        // 声明死信交换器: Dead Letter Exchange, DLX
        channel.exchangeDeclare("dlx-exchange", "fanout");
        // 声明死信队列: 跟死信交换器绑定的队列称为死信队列
        channel.queueDeclare("dlx-queue", true, false, false, null);
        // 绑定死信队列
        channel.queueBind("dlx-queue", "dlx-exchange", "");

        // 声明交换器
        channel.exchangeDeclare("ttl-exchange", "direct", true, false, null);
        // 队列参数
        Map<String, Object> arguments = new HashMap<>();
        // === 设置队列中消息的过期时间, 单位: 毫秒
        arguments.put("x-message-ttl", 60000);
        // 设置队列的过期时间, 队列在过期时间内未被使用则会被删除, 单位: 毫秒
        arguments.put("x-expires", 1800000);
        /* === 设置死信交换器, 消息变成死信的情况:
         -> 消息被拒绝(Basic.Reject/Basic.Nack), 并设置requeue参数为false
         -> 消息过期
         -> 队列达到最大长度
         */
        arguments.put("x-dead-letter-exchange", "dlx-exchange");
        // === 设置队列消息的最高优先级: 优先级队列
        arguments.put("x-max-priority", 10);
        // 声明队列
        channel.queueDeclare("ttl-queue", true, false, false, arguments);
        // 绑定队列与交换器
        channel.queueBind("ttl-queue", "ttl-exchange", "routing-key");

        // 消息的参数设置
        AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
        builder.deliveryMode(DeliveryMode.PERSISTENT); // 持久化消息
        builder.expiration("60000"); // 消息过期时间
        builder.priority(5); // 设置消息的优先级: 默认优先级为0
        /* === 发布消息:
         -> 消息最终的TTL = min(队列TTL, 消息TTL)
         -> 如果不设置TTL, 消息将不会过期; 如果TTL设置为0, 则除非可以直接将消息投递给消费者, 否则该消息将被立即丢弃
         -> 超过TTL未被消息的消息将变成"死信"(Dead Message)
         */
        channel.basicPublish("ttl-exchange", "routing-key", builder.build(), "TTL Message!".getBytes());

        // === 延迟队列: 使用TTL+死信队列实现, 直接消费死信队列即可
    }

}
