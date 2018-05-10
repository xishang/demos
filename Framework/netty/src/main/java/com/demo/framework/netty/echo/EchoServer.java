package com.demo.framework.netty.echo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/5/8
 */
public class EchoServer {

    private final int PORT = 8000;

    public static void main(String[] args) throws Exception {
        new EchoServer().start();
    }

    public void start() throws Exception {
        final EchoServerHandler serverHandler = new EchoServerHandler();
        EventLoopGroup group = new NioEventLoopGroup(); // 创建EventLoopGroup
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(group)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(PORT))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            // ServerBootstrap初始化之后往ChannelPipeline中添加ChannelHandler
                            socketChannel.pipeline().addLast(serverHandler);
                        }
                    });
            // 异步绑定服务器, 调用sync()方法阻塞直到绑定完成
            ChannelFuture future = bootstrap.bind().sync();
            // 获取Channel的CloseFuture, 并阻塞当前线程直到完成
            future.channel().closeFuture().sync();
        } finally {
            // 关闭EventLoopGroup, 释放所有资源
            group.shutdownGracefully().sync();
        }
    }

}
