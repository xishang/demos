package com.demo.framework.netty.extension;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/7/4
 */
public class Client {

    private final String host;
    private final int port;

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void run() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress(host, port))
                    .handler(new ClientInitializer());
            ChannelFuture future = bootstrap.connect().sync();
            // 获取通信channel
            Channel channel = future.channel();
            // 读取控制台输入的信息
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                // 向服务器发送信息
                CustomProtocol message = new CustomProtocol();
                message.setVersion(1);
                message.setContent(in.readLine());
                channel.writeAndFlush(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        new Client("localhost", 8080).run();
    }

}
