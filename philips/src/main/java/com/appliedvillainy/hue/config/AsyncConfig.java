package com.appliedvillainy.hue.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuration for asynchronous task execution.
 * Required for light animations to run in background threads.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "animationExecutor")
    public Executor animationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("animation-");
        executor.initialize();
        return executor;
    }
}
