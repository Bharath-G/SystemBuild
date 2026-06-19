package com.bharath.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.bharath.system.config.SystemProperties;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(SystemProperties.class)
public class SystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(SystemApplication.class, args);
    }
}
