package io.dobermoney.launchpool;

import io.dobermoney.launchpool.config.CoingeckoProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties(CoingeckoProperties.class)
@EnableScheduling
public class LaunchpoolServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(LaunchpoolServiceApplication.class, args);
    }

}
