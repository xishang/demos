package com.demos.java.basedemo.net.udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/2/24
 */
public class DatagramClient {

    public static void main(String[] args) throws Exception {
        /* 向服务端发送数据 */
        // 1.服务端地址和端口信息
        InetAddress address = InetAddress.getByName("localhost");
        int port = 8888;
        byte[] data = "你好，我是Datagram客户端！".getBytes("utf-8");
        // 2.创建DatagramPacket，向服务端发送数据
        DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
        // 3.创建DatagramSocket，用于发送数据报
        DatagramSocket socket = new DatagramSocket();
        // 4.发送数据
        socket.send(packet);

        /* 接收服务端响应 */
        byte[] receiveData = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        socket.receive(receivePacket);
        System.out.println("接收到服务端响应: " + new String(receiveData, 0, receivePacket.getLength(), "utf-8"));
        // 关闭资源
        socket.close();
    }

}
