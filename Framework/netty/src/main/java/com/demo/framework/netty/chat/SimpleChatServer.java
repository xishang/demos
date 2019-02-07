package com.demo.framework.netty.chat;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/7/3
 * <p>
 * 基于TCP的聊天服务
 */
public class SimpleChatServer {

    private int port;

    public SimpleChatServer(int port) {
        this.port = port;
    }

    public void run() throws Exception {
        // 用来接收进来的连接: acceptor线程池: 线程数为1
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        // 用来处理已经被接收的连接: 一旦`boss`接收到连接, 就会把连接信息注册到`worker`上
        // I/O线程池: worker线程数为默认线程数: 2 * Runtime.getRuntime().availableProcessors()
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            // 服务启动类
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) // 使用nio
                    .childHandler(new SimpleChatServerInitializer()) // 初始化器
                    // TCP参数: 用于临时存放已完成三次握手的请求的队列的最大长度
                    .option(ChannelOption.SO_BACKLOG, 128)
                    // 是否启用心跳保活机制
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            System.out.println("SimpleChatServer已启动");
            // 绑定端口, 开始接收进来的连接
            ChannelFuture f = b.bind(port).sync();
            for (int i = 0; i < 5; i++) {
                new Thread(() -> {
                    Thread t1 = Thread.currentThread();
                    System.out.println("thread = " + t1.getId() + ", in = " + f.channel().eventLoop().inEventLoop() + ", outside ===== ");
                    f.channel().eventLoop().execute(() -> {
                        Thread t = Thread.currentThread();
                        System.out.println("thread = " + t.getId() + ", inside ===== ");
                    });
                }).start();
            }
            // 等待服务器socket关闭: 用于优雅停机
            f.channel().closeFuture().sync();
        } finally {
            // 关闭服务器
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            System.out.println("SimpleChatServer已关闭");
        }
    }

    public static void main(String[] args) throws Exception {
        new SimpleChatServer(8080).run();
    }

}
