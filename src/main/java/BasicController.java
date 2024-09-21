/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.czx.testNetty.demos.web;

import com.alibaba.fastjson.JSONObject;
import com.czx.testNetty.server.NettyServer;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.net.InetSocketAddress;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
@RestController
@RequestMapping("/netty")
public class BasicController {

    @Autowired
    private NettyServer nettyServer;

    @GetMapping("/start")
    public String startNettyServer() {
        try {
            nettyServer.start();

            for (int i = 0; i < 10 ; i++) {
                JSONObject jsonMsg = new JSONObject();
                jsonMsg.put("iotId", String.valueOf(i));
                jsonMsg.put("heartbeatCheckTime","1692528528");
                run(jsonMsg.toJSONString());
            }
            return "Netty server started.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to start Netty server.";
        }
    }

    public void sendMessage(String message) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioDatagramChannel.class)
                    .handler(new ChannelInitializer<DatagramChannel>() {
                        @Override
                        public void initChannel(DatagramChannel ch) throws Exception {
                            // No need to add any specific handlers for this simple example
                        }
                    });

            ChannelFuture f = b.connect(new InetSocketAddress("127.0.0.1", 9999)).sync();

            // Send the message
            f.channel().writeAndFlush(Unpooled.copiedBuffer(message + System.getProperty("line.separator"), CharsetUtil.UTF_8));
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

    public void run(String message) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(new ChannelInitializer<DatagramChannel>() {
                        @Override
                        protected void initChannel(DatagramChannel ch) throws Exception {
                            // 客户端不需要特别的处理器，仅用于发送消息
                        }
                    });

            // 发送消息到服务器
            ChannelFuture f = b.connect(new InetSocketAddress("127.0.0.1", 9999)).sync();
            f.channel().writeAndFlush(new DatagramPacket(
                    Unpooled.copiedBuffer(message, CharsetUtil.UTF_8),
                    new InetSocketAddress("127.0.0.1", 9999)
            )).sync();

            // 等待关闭
            f.channel().closeFuture().await();
        } finally {
            group.shutdownGracefully();
        }
    }


}
