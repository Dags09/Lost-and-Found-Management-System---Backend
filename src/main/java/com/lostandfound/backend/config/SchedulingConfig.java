package com.lostandfound.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

// Turns on @Scheduled methods (used by ItemExpiryService).
@Configuration
@EnableScheduling
public class SchedulingConfig {

}