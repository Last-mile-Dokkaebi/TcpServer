package com.example.TcpServer.handler;

import com.example.TcpServer.domain.BatteryDao;
import com.example.TcpServer.domain.BatteryRepo;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

@Slf4j
@Component
@ChannelHandler.Sharable
@RequiredArgsConstructor
public class SaveDataHandler extends ChannelInboundHandlerAdapter {
    private int DATA_LENGTH = 1024;
    private ByteBuf buff;
    private final BatteryRepo batteryRepo;
    private HashMap<String, Channel> channels = new HashMap<>();
    @Value("protocol.server.key")
    private final String key;

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
        log.info("channelActive: " + remoteAddress);
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
            } else if (receive.startsWith("SS") && receive.endsWith("FF")) {
                String bikeNum = receive.substring(2,6);
                registerChannel(ctx,bikeNum);
                ctx.writeAndFlush(makeResponse(receive));
            } else if (receive.startsWith("CL") && receive.endsWith("FF") && checkValidServer(receive)) { // 킥보드 전원 끄기
                String bikeNum = receive.substring(2,6);
                Channel channel = findChannel(bikeNum);
                String response = "BB" + bikeNum + "SSFF";
                channel.writeAndFlush(Unpooled.wrappedBuffer(response.getBytes()));
                log.info("off: " + bikeNum);
            } else if (receive.startsWith("ST") && receive.endsWith("FF") && checkValidServer(receive)) { // 킥보드 전원 켜기
                String bikeNum = receive.substring(2, 6);
                Channel channel = channels.get(bikeNum);
                String response = "BB" + bikeNum + "PPFF";
                channel.writeAndFlush(Unpooled.wrappedBuffer(response.getBytes()));
                log.info("on: " + bikeNum);
            } else {
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

    private void registerChannel(ChannelHandlerContext ctx, String bikeNum) {
        if (channels.containsKey(bikeNum)){
            channels.replace(bikeNum, ctx.channel());
        } else {
            channels.put(bikeNum, ctx.channel());
            log.info("register bike: " + bikeNum + ", remote address: " + ctx.channel().remoteAddress());
        }
    }

    private ByteBuf makeResponse(String msg) {
        String start = msg.substring(6, 8);
        String bikeNum = msg.substring(2, 6);
        String response = "BB" + bikeNum + "OK" + start + "FF";
        log.info("server send: " + response);
        return Unpooled.wrappedBuffer(response.getBytes());
    }

    private Channel findChannel(String bikeNum) {
        if (channels.containsKey(bikeNum)){
            return channels.get(bikeNum);
        } else {
            throw new RuntimeException("bike와 연결된 채널이 존재하지 않습니다.");
        }
    }

    private boolean checkValidServer(String msg) {
        if (msg.substring(6, 8).equals(key)) {
            return true;
        }
        return false;
    }
}
