package com.example.TcpServer;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EchoServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        String message = (String)msg;
        Channel channel = ctx.channel();
        log.info("Response : " + message + "received\n");
        ctx.close();
        //channel.writeAndFlush("Response : " + message + "received\n");

    }
}
