package com.example.TcpServer.socket;

import com.example.TcpServer.handler.SaveDataHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NettyChannelInitializer extends ChannelInitializer<SocketChannel> {
    private final SaveDataHandler handler;

    // 클라이언트 소켓 채널이 생성될 때 호출
    @Override
    protected void initChannel(SocketChannel ch) {
        log.info("initChannel");
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(handler);
    }
}
