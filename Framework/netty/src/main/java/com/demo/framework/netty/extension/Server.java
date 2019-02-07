package com.demo.framework.netty.extension;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/7/4
 */
public class Server {

    private int port;

    public Server(int port) {
        this.port = port;
    }

    public void run() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            // 服务启动类
            ServerBootstrap b = new ServerBootstrap();
            b.group(group)
                    .channel(NioServerSocketChannel.class) // 使用nio
                    .childHandler(new ServerInitializer()) // 初始化Channel
                    // TCP参数: 用于临时存放已完成三次握手的请求的队列的最大长度
                    .option(ChannelOption.SO_BACKLOG, 128)
                    // 是否启用心跳保活机制
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            System.out.println("Server已启动");
            // 绑定端口, 开始接收进来的连接
            ChannelFuture f = b.bind(port).sync();
            // 等待服务器socket关闭: 用于优雅停机
            f.channel().closeFuture().sync();
        } finally {
            // 关闭服务器
            group.shutdownGracefully();
            System.out.println("SimpleChatServer已关闭");
        }
    }

    public static void main(String[] args) throws Exception {
        new Server(8080).run();
    }

}
