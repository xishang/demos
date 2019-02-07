package com.demo.framework.netty.extension;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/7/4
 */
public class CustomProtocolEncoder extends MessageToByteEncoder<CustomProtocol> {

    @Override
    protected void encode(ChannelHandlerContext context, CustomProtocol protocol, ByteBuf buf) throws Exception {
        buf.writeInt(protocol.getVersion());
        byte[] content = protocol.getContent().getBytes("UTF-8");
        buf.writeInt(content.length);
        buf.writeBytes(content);
    }

}
