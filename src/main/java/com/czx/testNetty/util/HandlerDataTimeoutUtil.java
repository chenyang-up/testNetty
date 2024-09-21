package com.czx.testNetty.util;

import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;


/**
 * HandlerDataTimeoutUtil 超时文件数据处理工具类
 *
 * @author chenzhongxin
 * @date 2024/8/26
 */

@Slf4j
public class HandlerDataTimeoutUtil {


    /**
     * 信息解析
     *
     * @param iotInfoMap 存储信息
     * @param jsonObject 解析的数据
     *
     * */
    public static void dataParser(String hostAddress, Map<String, Map<String, String>> iotInfoMap, JSONObject jsonObject) {
        Map<String, String> hostMap = iotInfoMap.get(hostAddress);
        String iotId = jsonObject.getString("iotId");
        String heartbeatCheckTime = jsonObject.getString("heartbeatCheckTime");
        if (hostMap == null) {
            hostMap = new HashMap<>();
            hostMap.put(iotId, heartbeatCheckTime);
            iotInfoMap.put(hostAddress, hostMap);
        } else {
            hostMap.put(iotId, heartbeatCheckTime);
        }
    }


    /**
     * 推送告警信息
     *
     * @param ctx 上下文对象,含有管道pipeline,通道channel,地址
     * */
    public void pushAlarm(ChannelHandlerContext ctx) {
        //推送信息

    }

}
