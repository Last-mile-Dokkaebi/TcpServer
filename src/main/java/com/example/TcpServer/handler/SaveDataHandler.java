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
    //@Value 필드 경우 object가 생성되고 난 후 주입되므로 private final 형식으로 정의를 하면 @RequiredArgsConstructor에 의해
    //자동주입시 해당 멤버가 Bean에 등록되지 않았다고 오류가 발생한다.
    @Value("${protocol.server.key}")
    private String key;

    // 핸들러가 생성될 때 호출되는 메소드
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        log.info("handlerAdded");
        buff = ctx.alloc().buffer(DATA_LENGTH);
    }

    // 핸들러가 제거될 때 호출되는 메소드
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        log.info("handlerRemoved");
        buff = null;
    }

    // 클라이언트와 연결되어 트래픽을 생성할 준비가 되었을 때 호출되는 메소드
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        String remoteAddress = ctx.channel().remoteAddress().toString();
        log.info("channelActive: " + remoteAddress);
    }

    /**
     * channelhandler는 channel끼리 공유되므로 서버 채널에서 연결이 종료될때 buff가 null이 되어 다른 킥보드
     * 채널이 버퍼를 읽으려고 시도할때 오류가 발생했다.
     */
    @Transactional
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg){
        ByteBuf mBuf = (ByteBuf) msg;
        buff = ctx.alloc().buffer(DATA_LENGTH);
        buff.writeBytes(mBuf);
        if(buff.isReadable()) {
            String receive = buff.readCharSequence(buff.readableBytes(),StandardCharsets.UTF_8).toString();
            buff = ctx.alloc().buffer(DATA_LENGTH);
            log.info("receive: "+ receive);
            if (receive.startsWith("AA") && receive.endsWith("FF")) {
                BatteryDao batteryDao = new BatteryDao(receive);
                batteryRepo.save(batteryDao.toEntity());
                ctx.writeAndFlush(Unpooled.wrappedBuffer("BB0001OKAAFF".getBytes()));
            } else if (receive.startsWith("SS") && receive.endsWith("FF")) {
                String bikeNum = receive.substring(2,6);
                registerChannel(ctx,bikeNum);
                ctx.writeAndFlush(makeResponse(receive));
            } else if (receive.startsWith("CL") && receive.endsWith("FF") && checkValidServer(receive)) { // 킥보드 전원 끄기
                String bikeNum = receive.substring(2,6);
                String server = bikeNum + "s";
                registerChannel(ctx,server);
                Channel channel = findChannel(bikeNum);
                String response = "BB" + bikeNum + "SSFF";
                channel.writeAndFlush(Unpooled.wrappedBuffer(response.getBytes()));
                log.info("off: " + bikeNum);
            } else if (receive.startsWith("ST") && receive.endsWith("FF") && checkValidServer(receive)) { // 킥보드 전원 켜기
                String bikeNum = receive.substring(2, 6);
                Channel channel = findChannel(bikeNum);
                String server = bikeNum + "s";
                registerChannel(ctx,server);
                String response = "BB" + bikeNum + "PPFF";
                channel.writeAndFlush(Unpooled.wrappedBuffer(response.getBytes()));
                log.info("on: " + bikeNum);
            } else if (receive.startsWith("BB") && receive.endsWith("FF")) {
                String server = receive.substring(2, 6);
                Channel channel = findChannel(server + "s");
                if(receive.substring(8,10).equals("PP"))
                    channel.writeAndFlush(Unpooled.wrappedBuffer("START".getBytes()));
                else if(receive.substring(8,10).equals("SS"))
                    channel.writeAndFlush(Unpooled.wrappedBuffer("CLOSE".getBytes()));
                channel.close();
            } else {
                log.info(receive);
            }
        }
        mBuf.release();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        ctx.disconnect();
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
        if (msg.substring(6, 12).equals(key)) {
            return true;
        }
        return false;
    }
}
