package com.example.TcpServer.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class Decoder extends ByteToMessageDecoder {
    private final int DATA_LENGTH = 40;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
//        if (in.readableBytes() < DATA_LENGTH) {
//            return;
//        }

        out.add(in.readBytes(in.readableBytes()));
    }
}
