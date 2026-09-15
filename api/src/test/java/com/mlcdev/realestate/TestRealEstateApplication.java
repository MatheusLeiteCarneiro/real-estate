package com.mlcdev.realestate;

import org.springframework.boot.SpringApplication;

public class TestRealEstateApplication {

    static void main(String[] args) {
        SpringApplication.from(RealEstateApplication::main).with(TestcontainersConfiguration.class).run(args);
    }
}
