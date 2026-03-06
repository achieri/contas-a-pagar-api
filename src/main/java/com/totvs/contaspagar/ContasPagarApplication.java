package com.totvs.contaspagar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ContasPagarApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContasPagarApplication.class, args);
    }
}
