package com.aditi.smartbuy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SmartBuyApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartBuyApplication.class, args);
    }

}
