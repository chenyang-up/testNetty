package com.czx.testNetty.server;

import com.czx.testNetty.config.UdpServerHeartbeatCheckConfig;
import com.czx.testNetty.handler.IoTHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * NettyServer
 *
 * @author chenzhongxin
 * @date 2024/8/26
 */

@Component
@Slf4j
public class NettyServer {

    @Resource
    private IoTHandler ioTHandler;

    @Resource
    private UdpServerHeartbeatCheckConfig udpServerHeartbeatCheckConfig;

    //服务端需要2个线程组  boss处理客户端连接  work进行客服端连接之后的处理
    private final EventLoopGroup boss = new NioEventLoopGroup();
    private final EventLoopGroup work = new NioEventLoopGroup();
    public void start() {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(new ChannelInitializer<DatagramChannel>() {
                        @Override
                        protected void initChannel(DatagramChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new LineBasedFrameDecoder(128))
                                    .addLast(ioTHandler);
                        }
                    });
    //深刻揭示出啦
            // 绑定端口并开始接收数据包
            Channel channel = bootstrap.bind(udpServerHeartbeatCheckConfig.getUdpHeartbeatIp(), udpServerHeartbeatCheckConfig.getUdpHeartbeatPort()).sync().channel();
            channel.closeFuture().await();
            log.info("udpServiceHeartbeatCheck started,port:{}...............................", udpServerHeartbeatCheckConfig.getUdpHeartbeatPort());
        }catch (Exception e){
            log.error(e.getMessage(), e);
        } finally {
            group.shutdownGracefully();
        }
    }
}
