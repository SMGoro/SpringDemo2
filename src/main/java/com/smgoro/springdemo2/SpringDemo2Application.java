package com.smgoro.springdemo2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;

@SpringBootApplication
public class SpringDemo2Application {

    public static void main(String[] args) {
        SpringApplication.run(SpringDemo2Application.class, args);
        File directory = new File("logs");
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

}
