package com.mlcdev.realestate.api;

import org.springframework.boot.SpringApplication;

public class TestApiApplication {

    static void main(String[] args) {
        SpringApplication.from(ApiApplication::main).with(TestcontainersConfiguration.class).run(args);
    }
}
