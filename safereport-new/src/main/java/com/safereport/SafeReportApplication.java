package com.safereport;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SafeReportApplication {
    public static void main(String[] args) {
        SpringApplication.run(SafeReportApplication.class, args);
    }
}
