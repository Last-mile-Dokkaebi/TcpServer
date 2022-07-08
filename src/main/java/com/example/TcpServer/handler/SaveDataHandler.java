package com.example.TcpServer.handler;

import com.example.TcpServer.domain.BatteryDao;
import com.example.TcpServer.domain.BatteryRepo;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@ChannelHandler.Sharable
@RequiredArgsConstructor
public class SaveDataHandler extends ChannelInboundHandlerAdapter {
    private int DATA_LENGTH = 1024;
    private ByteBuf buff;
    private final BatteryRepo batteryRepo;

    // 핸들러가 생성될 때 호출되는 메소드
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        buff = ctx.alloc().buffer(DATA_LENGTH);
    }

    // 핸들러가 제거될 때 호출되는 메소드
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        buff = null;
    }

    // 클라이언트와 연결되어 트래픽을 생성할 준비가 되었을 때 호출되는 메소드
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        String remoteAddress = ctx.channel().remoteAddress().toString();
        log.info("Remote Address: " + remoteAddress);
    }

    @Transactional
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg){
        ByteBuf mBuf = (ByteBuf) msg;
        buff.writeBytes(mBuf);
        if(buff.isReadable()) {
            String receive = buff.readCharSequence(buff.readableBytes(),StandardCharsets.UTF_8).toString();
            buff = ctx.alloc().buffer(DATA_LENGTH);
            log.info("receive: "+ receive);
            if (receive.startsWith("AA") && receive.endsWith("FF")) {
                BatteryDao batteryDao = new BatteryDao(receive);
                batteryRepo.save(batteryDao.toEntity());
                ctx.writeAndFlush(Unpooled.wrappedBuffer("BB0001OKAAFF".getBytes()));
                log.info("server send: BB0001OKAAFF");
            } else if (receive.substring(6, 8).equals("PP")) {
                ctx.writeAndFlush(Unpooled.wrappedBuffer("BB0001OKPPFF".getBytes()));
                log.info("server send: BB0001OKPPFF");
            } else if (receive.substring(6, 8).equals("SS")) {
                ctx.writeAndFlush(Unpooled.wrappedBuffer("BB0001OKSSFF".getBytes()));
                log.info("server send: BB0001OKSSFF");
            } else if (receive.substring(6, 8).equals("CC")) {
                ctx.writeAndFlush(Unpooled.wrappedBuffer("BB0001OKCCFF".getBytes()));
                log.info("server send: BB0001OKCCFF");
            } else if (receive.substring(6, 8).equals("DD")) {
                ChannelFuture f = ctx.writeAndFlush(Unpooled.wrappedBuffer("BB0001OKDDFF".getBytes()));
                f.addListener(ChannelFutureListener.CLOSE);
            } else if (!receive.startsWith("AA")) {
                ctx.disconnect();
            }
        }
        mBuf.release();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        ctx.close();
        cause.printStackTrace();
    }

}
