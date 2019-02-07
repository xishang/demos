package com.demos.java.basedemo.net.multicast;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/6/23
 */
public class MulticastReceiver {

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
            byte[] readBytes = new byte[1024];
            while (true) {
                DatagramPacket packet = new DatagramPacket(readBytes, readBytes.length);
                // 接受数据
                socket.receive(packet);
                // 打印输出
                System.out.println("接收到组播消息: " + new String(packet.getData(), 0, packet.getLength()));
            }
        } finally {
            if (socket != null) {
                // 关闭socket
                socket.close();
            }
        }
    }

}
