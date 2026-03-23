package com.hackhaton.fctwaterbilling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class FctWaterBillingApplication {

    public static void main(String[] args) {
        SpringApplication.run(FctWaterBillingApplication.class, args);
    }

}
