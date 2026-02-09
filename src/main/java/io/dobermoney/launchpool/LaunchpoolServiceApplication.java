package io.dobermoney.launchpool;

import io.dobermoney.launchpool.config.properties.CoingeckoProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the Launchpool calculator application.
 * Provides crypto tools including average balance calculation for launchpool staking periods.
 */
@SpringBootApplication
@EnableConfigurationProperties(CoingeckoProperties.class)
@EnableScheduling
public class LaunchpoolServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(LaunchpoolServiceApplication.class, args);
    }

}
