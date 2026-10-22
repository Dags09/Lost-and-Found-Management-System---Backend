package com.lostandfound.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Turns on @Async, so emails go out on a background thread and a slow SMTP server never delays
 * an API request.
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}