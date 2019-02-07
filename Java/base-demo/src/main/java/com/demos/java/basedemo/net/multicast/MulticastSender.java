package com.demos.java.basedemo.net.multicast;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/6/23
 * <p>
 * 组播通信: MulticastSocket: 只会向组内成员发送信息, 针对性强
 * 广播通信: DatagramSocket: 向路由器连接的所有主机发送消息而不管它们是否需要, 存在网络资源的浪费, 但不用维护组成员
 * === 广播通信的用法跟UDP通信一致, 区别是发送的地址为广播地址
 * === 广播通信只能在局域网内传播, 组播通信可以在公网内传播
 */
public class MulticastSender {

    // 发送组播消息使用的端口
    private static final int PORT = 8000;
    // 组播地址
    private static final String GROUP_ADDRESS = "288.0.0.4";

    public static void main(String[] args) throws Exception {
        MulticastSocket socket = null;
        try {
            // 获取组播地址
            InetAddress group = InetAddress.getByName(GROUP_ADDRESS);
            // 创建MulticastSocket: 使用8000端口
            socket = new MulticastSocket(PORT);
            // 加入组
            socket.joinGroup(group);
            // 构建数据包
            byte[] messageBytes = "Hello, Multicast!".getBytes();
            DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, group, PORT);
            // 向组播发送数据包
            socket.send(packet);
        } finally {
            if (socket != null) {
                // 关闭socket
                socket.close();
            }
        }
    }

}
