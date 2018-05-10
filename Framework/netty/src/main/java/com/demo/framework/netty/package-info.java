/**
 * @author xishang
 * @version 1.0
 * @since 2018/5/10
 * <p>
 * Netty组件设计:
 * <p>
 * -> 生命周期管理: EventLoopGroup, EventLoop, Channel
 * 1.一个`EventLoopGroup`包含多个`EventLoop`
 * 2.一个`EventLoop`在它的生命周期内只绑定一个`Thread`
 * 3.一个`Channel`在它的生命周期内只注册到一个`EventLoop`
 * 4.一个`EventLoop`可被分配给多个`Channel`
 * 5.所有`EventLoop`处理的I/O事件都在它专有的`Thread`上处理
 * <p>
 * -> 数据流管理: ChannelPipeline, ChannelHandler, ChannelHandlerContext
 * <p>
 * Channel类型:
 * -> NioSocketChannel
 * -> NioServerSocketChannel
 * -> NioDatagramChannel
 * -> LocalServerChannel
 * -> EmbeddedChannel
 * <p>
 * EventLoopGroup类型:
 * -> NioEventLoopGroup
 * -> OioEventLoopGroup
 * <p>
 * ByteBuf: readIndex, writeIndex, capacity
 */
package com.demo.framework.netty;