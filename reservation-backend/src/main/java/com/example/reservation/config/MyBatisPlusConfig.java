package com.example.reservation.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.example.reservation.mapper")
public class MyBatisPlusConfig {
}

