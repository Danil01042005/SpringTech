package ru.danil.springtech;

import org.springframework.boot.SpringApplication;

public class TestSpringTechApplication {

    public static void main(String[] args) {
        SpringApplication.from(SpringTechApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
