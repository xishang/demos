package com.demo.framework.netty.chat;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/7/3
 */
public class SimpleChatClientInitializer extends ChannelInitializer<SocketChannel> {

    /**
     * 初始化客户端连接
     *
     * @param channel
     * @throws Exception
     */
    @Override
    public void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();
        // 粘包/拆包处理: 和服务器端保持一致
        pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
        pipeline.addLast("decoder", new StringDecoder());
        pipeline.addLast("encoder", new StringEncoder());
        pipeline.addLast("handler", new SimpleChatClientHandler());
    }

}
