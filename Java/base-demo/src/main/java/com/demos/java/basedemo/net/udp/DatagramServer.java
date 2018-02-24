package com.demos.java.basedemo.net.udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/2/24
 * <p>
 * UDP通信: 数据报
 * 无连接、数据包包含地址信息
 */
public class DatagramServer {

    public static void main(String[] args) throws Exception {
        /* 服务端接收数据 */
        // 1.创建DatagramSocket，指定端口
        DatagramSocket socket = new DatagramSocket(8888);
        // 2.创建DatagramPacket用于接收数据包
        byte[] data = new byte[1024];
        DatagramPacket packet = new DatagramPacket(data, data.length);
        // 3.接收数据
        socket.receive(packet);
        // 4.输出数据: 长度为数据包的长度
        System.out.println("接收到数据: " + new String(data, 0, packet.getLength(), "utf-8"));

        /* 响应客户端 */
        // 客户端地址和端口信息
        InetAddress clientAddress = packet.getAddress();
        int clientPort = packet.getPort();
        byte[] sendData = "你好，已收到你的信息！".getBytes("utf-8");
        // 要发送的数据包: 包含地址信息
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
        // 发送数据
        socket.send(sendPacket);
        // 关闭资源
        socket.close();
    }

}
