package com.hackhaton.fctbillingweb.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "com.hackhaton.fctbillingweb",     // Vaadin views, web layer
        "com.hackhaton.fctwaterbilling"    // entities, repos, services from USSD module
})
@EntityScan("com.hackhaton.fctwaterbilling.entity")
@EnableJpaRepositories("com.hackhaton.fctwaterbilling.repository")
public class WebApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }
}
