package com.czx.testNetty.handler;

import com.alibaba.fastjson.JSONObject;
import com.czx.testNetty.config.UdpServerHeartbeatCheckConfig;
import com.czx.testNetty.util.HandlerDataTimeoutUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * 消息处理器
 *
 * @author chenzhongxin
 * @date 2024/8/26
 */

@Component
@ChannelHandler.Sharable
@Slf4j
public class IoTHandler extends SimpleChannelInboundHandler {

    //共享的iot Map信息
    private Map<String, Map<String, String>> iotInfoMap = Collections.synchronizedMap(new HashMap<>());

    @Resource
    private UdpServerHeartbeatCheckConfig udpServerHeartbeatCheckConfig;

    /**
     * 工程异常时调用
     *
     * */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) {
        log.error("客户端'{}'异常,消息异常,isActive:{}", getIp(ctx), ctx.channel().isActive(), e);
        ctx.channel().close();
    }

    /**
     * 通道激活时触发
     * */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        log.info("ACTIVE: " + ctx.channel().remoteAddress());
    }

    /**
     * 当通道不处于活动状态时（连接已关闭）调用。
     * */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String hostAddress = getIp(ctx);
        Map<String, String> iotMap = iotInfoMap.get(hostAddress);
        iotMap.keySet().forEach(key -> {
            Boolean whetherTimeout = determineTimeout(iotMap.get(key));
            if (whetherTimeout) {
                //FIXME 推送告警信息
            }
        });

        super.channelInactive(ctx);
        log.info("INACTIVE: " + ctx.channel().remoteAddress());
        log.info("ctx close....");
        ctx.channel().close();
        ctx.disconnect();
        ctx.close();
        iotInfoMap.clear();

    }


    /**
     * 当收到消息时触发
     * */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            String hostAddress = getIp(ctx);
            ByteBuf byteBuf = (ByteBuf) msg;
            String string = byteBuf.toString(CharsetUtil.UTF_8);

            //消息转换成byte[]
//            byte[] bytes = transferAsciiToHex(ctx, byteBuf);

//            JSONObject jsonMsg = JSONObject.parseObject(msg.toString());
//            log.info("msg:{}", jsonMsg.toJSONString());
            //HandlerDataTimeoutUtil.dataParser(hostAddress, iotInfoMap, jsonMsg);
            log.info("iotInfoMap:" + iotInfoMap.toString());
        } catch (Exception e) {
            log.error("IoTHandler->channelRead0异常：", e);
        }
    }


    /**
     * 获取ip地址
     *
     * @param ctx ctx
     * @return String
     * @author zhangyy
     */
    private String getIp(ChannelHandlerContext ctx) {
        InetSocketAddress inetSocketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        String ip = inetSocketAddress.getAddress().getHostAddress();
        return ip;
    }

    /**
     * 超时判断
     *
     * @param timestampStr iot传递的时间戳
     * @return Boolean
     * */
    private Boolean determineTimeout (String timestampStr) {
        //当前时间戳
        long currentTime = System.currentTimeMillis();
        long timestamp = Long.parseLong(timestampStr);
        long timeDifference = currentTime - timestamp;
        //转换成分钟
        long timeDifferenceInMinutes = timeDifference / (1000 * 60);
        if (udpServerHeartbeatCheckConfig.getTimeoutDuration() > timeDifferenceInMinutes) {
            return false;
        }
        return true;
    }

    /**
     * 如果是iot传过来的消息，要从ascii转为16进制
     *
     * @param ctx     ctx
     * @param byteBuf byteBuf
     */
    private byte[] transferAsciiToHex(ChannelHandlerContext ctx, ByteBuf byteBuf) {
        InetSocketAddress inetSocketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        String hostAddress = inetSocketAddress.getAddress().getHostAddress();
        //不是内部地址，就要进行ascii转16进制
        byte[] resultBytes = ByteBufUtil.hexDump(byteBuf).getBytes();
        log.info("收到：{}, 转换后的消息是：{}", hostAddress, new String(resultBytes));
        return resultBytes;
    }
}
