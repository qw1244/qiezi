package com.cms.video;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan(basePackages = "com.cms.video.modules", annotationClass = Mapper.class)
@EnableScheduling
public class CmsVideoApp {

    public static void main(String[] args) {
        SpringApplication.run(CmsVideoApp.class, args);
    }
}
