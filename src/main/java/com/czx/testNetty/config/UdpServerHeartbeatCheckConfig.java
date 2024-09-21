package com.czx.testNetty.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 配置信息
 *
 * @author chenzhongxin
 * @date 2024/8/26
 */
@Component
@Data
public class UdpServerHeartbeatCheckConfig {

    //端口
    @Value("${netty.udp_heartbeat_port}")
    private int udpHeartbeatPort;

    //IP
    @Value("${netty.udp_heartbeat_ip}")
    private String udpHeartbeatIp;

    //超时时间约束
    @Value("${netty.udp_heartbeat_timeout}")
    private int TimeoutDuration;

}
