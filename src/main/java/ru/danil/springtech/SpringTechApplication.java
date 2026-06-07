package ru.danil.springtech;
import org.jobrunr.configuration.JobRunr;
import org.jobrunr.scheduling.JobScheduler;
import org.jobrunr.server.JobActivator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.annotation.EnableRetry;

import javax.sql.DataSource;

@SpringBootApplication
@EnableCaching
@EnableFeignClients
@EnableRetry
public class SpringTechApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringTechApplication.class, args);
    }
}
