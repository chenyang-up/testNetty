package com.czx.testNetty;

import com.czx.testNetty.server.NettyServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.Resource;

@SpringBootApplication
@Slf4j
public class TestNettyApplication implements CommandLineRunner {

    @Resource
    private NettyServer nettyServer;
    public static void main(String[] args) {
        SpringApplication.run(TestNettyApplication.class, args);
    }

    @Override
    public void run(String... args) {
        //启动netty
//        try {
//            nettyServer.start();
//
//        } catch (Exception e) {
//            log.error("", e);
//        }
    }
}
