package com.onlinetrain;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 在线培训系统启动类
 */
@SpringBootApplication
@MapperScan("com.onlinetrain.mapper")
public class OnlineTrainApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnlineTrainApplication.class, args);
    }
}
