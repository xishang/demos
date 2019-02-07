package com.demo.framework.netty.extension;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/7/4
 */
public class CustomProtocolDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext context, ByteBuf buf, List<Object> list) throws Exception {
        int version = buf.readInt();
        int length = buf.readInt();
        // 使用length长度的byte数组接受消息体
        byte[] bytes = new byte[length];
        // 读取消息体
        buf.readBytes(bytes);
        String content = new String(bytes, "UTF-8");
        CustomProtocol protocol = new CustomProtocol();
        protocol.setVersion(version);
        protocol.setContentLength(length);
        protocol.setContent(content);
        list.add(protocol);
    }

}
