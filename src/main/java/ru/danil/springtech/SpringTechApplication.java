package ru.danil.springtech;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableCaching
@EnableFeignClients
@EnableRetry
public class SpringTechApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringTechApplication.class, args);
    }
}
